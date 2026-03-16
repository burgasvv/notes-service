package org.burgas.service

import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.burgas.database.*
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection
import java.util.*

fun IdentityEntity.insert(identityRequest: IdentityRequest) {
    this.authority = identityRequest.authority ?: Authority.USER
    this.username = identityRequest.username ?: throw IllegalArgumentException("Identity username is null")
    this.password = if (!identityRequest.password.isNullOrEmpty()) BCrypt.hashpw(
        identityRequest.password, BCrypt.gensalt()
    ) else throw IllegalArgumentException("Identity password is null or empty")
    this.email = identityRequest.email ?: throw IllegalArgumentException("Identity email is null")
    this.enabled = identityRequest.enabled ?: true
    this.firstname = identityRequest.firstname ?: throw IllegalArgumentException("Identity firstname is null")
    this.lastname = identityRequest.lastname ?: throw IllegalArgumentException("Identity lastname is null")
    this.patronymic = identityRequest.patronymic ?: throw IllegalArgumentException("Identity patronymic is null")
}

fun IdentityEntity.update(identityRequest: IdentityRequest) {
    this.authority = identityRequest.authority ?: this.authority
    this.username = identityRequest.username ?: this.username
    this.email = identityRequest.email ?: this.email
    this.firstname = identityRequest.firstname ?: this.firstname
    this.lastname = identityRequest.lastname ?: this.lastname
    this.patronymic = identityRequest.patronymic ?: this.patronymic
}

fun IdentityEntity.toIdentityShortResponse(): IdentityShortResponse {
    return IdentityShortResponse(
        id = this.id.value,
        username = this.username,
        email = this.email,
        firstname = this.firstname,
        lastname = this.lastname,
        patronymic = this.patronymic,
        image = this.image?.toImageResponse()
    )
}

fun IdentityEntity.toIdentityFullResponse(): IdentityFullResponse {
    return IdentityFullResponse(
        id = this.id.value,
        username = this.username,
        email = this.email,
        firstname = this.firstname,
        lastname = this.lastname,
        patronymic = this.patronymic,
        image = this.image?.toImageResponse(),
        notes = this.notes.map { it.toNoteShortResponse() }
    )
}

class IdentityService {

    val imageService: ImageService = ImageService()
    val redis = DatabaseFactory.redis

    val identityKey: String = "identityFullResponse::%s"
    val noteKey: String = "noteFullResponse::%s"

    suspend fun create(identityRequest: IdentityRequest) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityFullResponse = IdentityEntity.new { this.insert(identityRequest) }.toIdentityFullResponse()
        val identityKey = identityKey.format(identityFullResponse.id)
        redis.set(identityKey, Json.encodeToString(identityFullResponse))
        identityFullResponse
    }

    suspend fun findAll() = newSuspendedTransaction(
        db = DatabaseFactory.postgres, context = Dispatchers.Default, readOnly = true
    ) {
        IdentityEntity.all().with(IdentityEntity::image).map { it.toIdentityShortResponse() }
    }

    suspend fun findById(identityId: UUID) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        readOnly = true
    ) {
        val identityKey = identityKey.format(identityId)
        if (redis.exists(identityKey)) {
            Json.decodeFromString<IdentityFullResponse>(redis.get(identityKey))
        } else {
            val identityFullResponse =
                (IdentityEntity.findById(identityId) ?: throw IllegalArgumentException("Identity not found"))
                    .load(IdentityEntity::image, IdentityEntity::notes)
                    .toIdentityFullResponse()
            redis.set(identityKey, Json.encodeToString(identityFullResponse))
            identityFullResponse
        }
    }

    suspend fun update(identityRequest: IdentityRequest) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = IdentityEntity.findByIdAndUpdate(
            identityRequest.id ?: throw IllegalArgumentException("Identity id is null")
        ) { it.update(identityRequest) } ?: throw IllegalArgumentException("Identity not found and not updated")

        handleCache(identityEntity)
    }

    suspend fun delete(identityId: UUID) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = (IdentityEntity.findById(identityId) ?: throw IllegalArgumentException("Identity not found"))
        identityEntity.delete()
        handleCache(identityEntity)
    }

    suspend fun changePassword(identityRequest: IdentityRequest) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        if (identityRequest.id == null) throw IllegalArgumentException("Identity Request id is null")
        if (identityRequest.password.isNullOrEmpty()) throw IllegalArgumentException("Identity Request password is null")

        val identityEntity = (IdentityEntity.findById(identityRequest.id) ?: throw IllegalArgumentException("Identity not found"))
        if (!BCrypt.checkpw(identityRequest.password, identityEntity.password)) {
            identityEntity.apply { this.password = BCrypt.hashpw(identityRequest.password, BCrypt.gensalt()) }

        } else {
            throw IllegalArgumentException("New and old passwords matched")
        }
        handleCache(identityEntity)
    }

    suspend fun changeStatus(identityRequest: IdentityRequest) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        if (identityRequest.id == null) throw IllegalArgumentException("Identity Request id is null")
        if (identityRequest.enabled == null) throw IllegalArgumentException("Identity Request status is null")

        val identityEntity = (IdentityEntity.findById(identityRequest.id) ?: throw IllegalArgumentException("Identity not found"))
        if (identityEntity.enabled != identityRequest.enabled) {
            identityEntity.apply { this.enabled = identityRequest.enabled }

        } else {
            throw IllegalArgumentException("Statuses matched")
        }
        handleCache(identityEntity)
    }

    suspend fun uploadImage(identityId: UUID, multiPartData: MultiPartData) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = (IdentityEntity.findById(identityId) ?: throw IllegalArgumentException("Identity not found"))
        val imageEntity = imageService.uploadIdentityImage(multiPartData)
        identityEntity.apply { this.image = imageEntity }
        handleCache(identityEntity)
    }

    suspend fun removeImage(identityId: UUID) = newSuspendedTransaction(
        db = DatabaseFactory.postgres,
        context = Dispatchers.Default,
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = (IdentityEntity.findById(identityId) ?: throw IllegalArgumentException("Identity not found"))
        val image = identityEntity.image
        if (image != null) {
            imageService.removeImages(listOf(image.id.value))
        } else {
            throw IllegalArgumentException("Identity image is null")
        }
    }

    private fun handleCache(identityEntity: IdentityEntity) {
        val identityKey = identityKey.format(identityEntity.id.value)
        if (redis.exists(identityKey)) redis.del(identityKey)

        if (!identityEntity.notes.empty()) {
            identityEntity.notes.forEach { noteEntity ->
                val noteKey = noteKey.format(noteEntity.id.value)
                if (redis.exists(noteKey)) redis.del(noteKey)
            }
        }
    }
}