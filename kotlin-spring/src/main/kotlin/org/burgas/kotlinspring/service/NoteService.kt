package org.burgas.kotlinspring.service

import jakarta.servlet.http.Part
import org.burgas.kotlinspring.entity.identity.IdentityFullResponse
import org.burgas.kotlinspring.entity.note.Note
import org.burgas.kotlinspring.entity.note.NoteFullResponse
import org.burgas.kotlinspring.entity.note.NoteRequest
import org.burgas.kotlinspring.entity.note.NoteShortResponse
import org.burgas.kotlinspring.mapper.NoteMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
class NoteService : CrudService<NoteRequest, Note, NoteShortResponse, NoteFullResponse> {

    private final val noteMapper: NoteMapper
    private final val imageService: ImageService

    @Qualifier(value = "identityRedisTemplate")
    private final val identityRedisTemplate: RedisTemplate<String, IdentityFullResponse>

    @Qualifier(value = "noteRedisTemplate")
    private final val noteRedisTemplate: RedisTemplate<String, NoteFullResponse>

    private final val identityKey: String = "identityFullResponse::%s"
    private final val noteKey: String = "noteFullResponse::%s"

    constructor(
        noteMapper: NoteMapper,
        identityRedisTemplate: RedisTemplate<String, IdentityFullResponse>,
        noteRedisTemplate: RedisTemplate<String, NoteFullResponse>,
        imageService1: ImageService
    ) {
        this.noteMapper = noteMapper
        this.identityRedisTemplate = identityRedisTemplate
        this.noteRedisTemplate = noteRedisTemplate
        this.imageService = imageService1
    }

    private fun handleCache(note: Note) {
        val noteKey: String = this.noteKey.format(note.id)
        if (this.noteRedisTemplate.hasKey(noteKey)) this.noteRedisTemplate.delete(noteKey)

        val identity = note.identity
        val identityKey: String = this.identityKey.format(identity.id)
        if (this.identityRedisTemplate.hasKey(identityKey)) this.identityRedisTemplate.delete(identityKey)
    }

    override fun findEntity(id: UUID): Note {
        return this.noteMapper.noteRepository.findById(id)
            .orElseThrow { throw IllegalArgumentException("Note not found") }
    }

    override fun findAll(): List<NoteShortResponse> {
        return this.noteMapper.noteRepository.findAll()
            .map { this.noteMapper.toShortResponse(it) }
    }

    override fun findById(id: UUID): NoteFullResponse {
        val noteKey = this.noteKey.format(id)
        val responseFromRedis = this.noteRedisTemplate.opsForValue()[noteKey]
        if (responseFromRedis != null) {
            return responseFromRedis

        } else {
            val responseFromPostgres = this.noteMapper.toFullResponse(this.findEntity(id))
            this.noteRedisTemplate.opsForValue().set(noteKey, responseFromPostgres)
            return responseFromPostgres
        }
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    override fun create(request: NoteRequest) {
        val note = this.noteMapper.toEntity(request)
        this.noteMapper.noteRepository.save(note)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    override fun update(request: NoteRequest) {
        if (request.id == null) throw IllegalArgumentException("Note request id is null")
        val note = this.noteMapper.toEntity(request)
        this.noteMapper.noteRepository.save(note)
        handleCache(note)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    override fun delete(id: UUID) {
        val note = this.findEntity(id)
        if (!note.images.isEmpty()) {
            note.images.forEach { image ->
                this.imageService.remove(image)
            }
        }
        this.noteMapper.noteRepository.delete(note)
        handleCache(note)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    fun addImages(noteId: UUID, parts: List<Part>) {
        val note = this.findEntity(noteId)
        parts.forEach { part ->
            val image = this.imageService.upload(part)
            note.images.add(image)
        }
        handleCache(note)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    fun removeImages(noteId: UUID, listImages: List<UUID>) {
        val note = this.findEntity(noteId)
        note.images.filter { image -> listImages.contains(image.id) }
            .forEach { image ->
                this.imageService.remove(image)
            }
        handleCache(note)
    }
}