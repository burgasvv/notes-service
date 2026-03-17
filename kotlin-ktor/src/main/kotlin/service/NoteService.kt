package org.burgas.service

import io.ktor.http.content.MultiPartData
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.json.Json
import org.burgas.database.*
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread

fun NoteEntity.insert(noteRequest: NoteRequest) {
    this.title = noteRequest.title ?: throw IllegalArgumentException("Note title is null")
    this.content = noteRequest.content ?: throw IllegalArgumentException("Note content is null")
    this.createdAt = LocalDateTime.now().toKotlinLocalDateTime()
    this.identity =
        IdentityEntity.findById(noteRequest.identityId ?: throw IllegalArgumentException("Note identityId is null"))
            ?: throw IllegalArgumentException("Note identity not found")
}

fun NoteEntity.update(noteRequest: NoteRequest) {
    this.title = noteRequest.title ?: this.title
    this.content = noteRequest.content ?: this.content
    this.identity = IdentityEntity.findById(noteRequest.identityId ?: UUID(0, 0)) ?: this.identity
}

fun NoteEntity.toNoteShortResponse(): NoteShortResponse {
    return NoteShortResponse(
        id = this.id.value,
        title = this.title,
        content = this.content,
        createdAt = this.createdAt.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm")),
        images = this.images.map { it.toImageResponse() }
    )
}

fun NoteEntity.toNoteFullResponse(): NoteFullResponse {
    return NoteFullResponse(
        id = this.id.value,
        title = this.title,
        content = this.content,
        createdAt = this.createdAt.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm")),
        identity = this.identity.toIdentityShortResponse(),
        images = this.images.map { it.toImageResponse() }
    )
}

class NoteService : CrudService<NoteRequest, NoteShortResponse, NoteFullResponse> {

    val imageService = ImageService()
    val redis = DatabaseFactory.redis

    val noteKey = "noteFullResponse::%s"
    val identityKey = "identityFullResponse::%s"

    override suspend fun create(request: NoteRequest): NoteFullResponse = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val noteFullResponse = NoteEntity.new { this.insert(request) }
            .load(NoteEntity::identity, NoteEntity::images)
            .toNoteFullResponse()
        val noteKey = noteKey.format(noteFullResponse.id)
        redis.set(noteKey, Json.encodeToString(noteFullResponse))
        noteFullResponse
    }

    override suspend fun findAll(): List<NoteShortResponse> = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        readOnly = true
    ) {
        NoteEntity.all().with(NoteEntity::images).map { it.toNoteShortResponse() }
    }

    override suspend fun findById(id: UUID): NoteFullResponse = newSuspendedTransaction {
        val noteKey = noteKey.format(id)
        if (redis.exists(noteKey)) {
            Json.decodeFromString<NoteFullResponse>(redis.get(noteKey))
        } else {
            val noteFullResponse = (NoteEntity.findById(id) ?: throw IllegalArgumentException("Note not found"))
                .load(NoteEntity::identity, NoteEntity::images)
                .toNoteFullResponse()
            redis.set(noteKey, Json.encodeToString(noteFullResponse))
            noteFullResponse
        }
    }

    override suspend fun update(request: NoteRequest) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val noteEntity =
            NoteEntity.findByIdAndUpdate(
                request.id ?: throw IllegalArgumentException("Note request id is null")
            ) { it.update(request) } ?: throw IllegalArgumentException("Note not found")

        handleCache(noteEntity)
    }

    override suspend fun delete(id: UUID) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val noteEntity = (NoteEntity.findById(id) ?: throw IllegalArgumentException("Note not found"))
        noteEntity.images.forEach { imageEntity ->
            imageEntity.delete()
        }
        noteEntity.delete()
        handleCache(noteEntity)
    }

    suspend fun uploadImages(noteId: UUID, multiPartData: MultiPartData) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val noteEntity = (NoteEntity.findById(noteId) ?: throw IllegalArgumentException("Note not found"))
            .load(NoteEntity::identity, NoteEntity::images)

        val imageEntities = imageService.uploadNoteImages(multiPartData)
        noteEntity.images = SizedCollection(noteEntity.images + imageEntities)
        handleCache(noteEntity)
    }

    suspend fun removeImages(noteId: UUID, imageIds: List<UUID>) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val noteEntity = (NoteEntity.findById(noteId) ?: throw IllegalArgumentException("Note not found"))
            .load(NoteEntity::identity, NoteEntity::images)

        if (noteEntity.images.map { it.id.value }.containsAll(imageIds)) {
            noteEntity.images.filter { imageEntity -> imageIds.contains(imageEntity.id.value) }
                .forEach { imageEntity -> imageEntity.delete() }
        } else {
            throw IllegalArgumentException("Not all images can be deleted")
        }
        handleCache(noteEntity)
    }

    private fun handleCache(noteEntity: NoteEntity) {
        val noteKey = noteKey.format(noteEntity.id.value)
        if (redis.exists(noteKey)) redis.del(noteKey)

        val identityKey = identityKey.format(noteEntity.identity.id.value)
        if (redis.exists(identityKey)) redis.del(identityKey)
    }
}