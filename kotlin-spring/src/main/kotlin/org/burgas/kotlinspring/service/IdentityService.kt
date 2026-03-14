package org.burgas.kotlinspring.service

import jakarta.servlet.http.Part
import org.burgas.kotlinspring.entity.identity.Identity
import org.burgas.kotlinspring.entity.identity.IdentityFullResponse
import org.burgas.kotlinspring.entity.identity.IdentityRequest
import org.burgas.kotlinspring.entity.identity.IdentityShortResponse
import org.burgas.kotlinspring.entity.note.NoteFullResponse
import org.burgas.kotlinspring.kafka.CustomKafkaProducer
import org.burgas.kotlinspring.mapper.IdentityMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
class IdentityService : CrudService<IdentityRequest, Identity, IdentityShortResponse, IdentityFullResponse> {

    private final val identityMapper: IdentityMapper
    private final val imageService: ImageService

    @Qualifier(value = "identityRedisTemplate")
    private final val identityRedisTemplate: RedisTemplate<String, IdentityFullResponse>

    @Qualifier(value = "noteRedisTemplate")
    private final val noteRedisTemplate: RedisTemplate<String, NoteFullResponse>

    private final val passwordEncoder: PasswordEncoder

    private final val customKafkaProducer: CustomKafkaProducer

    private val identityKey: String = "identityFullResponse::%s"
    private val noteKey: String = "noteFullResponse::%s"

    constructor(
        identityMapper: IdentityMapper,
        imageService: ImageService,
        identityRedisTemplate: RedisTemplate<String, IdentityFullResponse>,
        noteRedisTemplate: RedisTemplate<String, NoteFullResponse>,
        passwordEncoder: PasswordEncoder,
        customKafkaProducer: CustomKafkaProducer
    ) {
        this.identityMapper = identityMapper
        this.imageService = imageService
        this.identityRedisTemplate = identityRedisTemplate
        this.noteRedisTemplate = noteRedisTemplate
        this.passwordEncoder = passwordEncoder
        this.customKafkaProducer = customKafkaProducer
    }

    private fun handleCache(identity: Identity) {
        val identityKey = this.identityKey.format(identity.id)
        if (identityRedisTemplate.hasKey(identityKey)) identityRedisTemplate.delete(identityKey)

        if (!identity.notes.isEmpty()) {

            identity.notes.forEach { note ->
                val noteKey = this.noteKey.format(note.id)
                if (noteRedisTemplate.hasKey(noteKey)) noteRedisTemplate.delete(noteKey)
            }
        }
    }

    override fun findEntity(id: UUID): Identity {
        return this.identityMapper.identityRepository.findById(id)
            .orElseThrow { throw IllegalArgumentException("Identity not found") }
    }

    override fun findAll(): List<IdentityShortResponse> {
        return this.identityMapper.identityRepository.findAll()
            .map { this.identityMapper.toShortResponse(it) }
    }

    override fun findById(id: UUID): IdentityFullResponse {
        val identityKey = this.identityKey.format(id)
        val identityFullResponse = this.identityRedisTemplate.opsForValue().get(identityKey)

        if (identityFullResponse != null) {
            return identityFullResponse

        } else {
            val response = this.identityMapper.toFullResponse(this.findEntity(id))
            this.identityRedisTemplate.opsForValue().set(identityKey, response)
            return response
        }
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    override fun create(request: IdentityRequest) {
        var identity = this.identityMapper.toEntity(request)
        identity = this.identityMapper.identityRepository.save(identity)
        val identityFullResponse = this.identityMapper.toFullResponse(identity)
        this.customKafkaProducer.sendIdentityFullResponse(identityFullResponse)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    override fun update(request: IdentityRequest) {
        if (request.id == null) throw IllegalArgumentException("Identity id is null")
        val identity = this.identityMapper.toEntity(request)
        this.identityMapper.identityRepository.save(identity)
        handleCache(identity)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    override fun delete(id: UUID) {
        val identity = this.findEntity(id)
        this.identityMapper.identityRepository.delete(identity)
        handleCache(identity)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    fun uploadImage(identityId: UUID, part: Part) {
        val identity = this.findEntity(identityId)
        val image = this.imageService.upload(part)
        identity.apply {
            this.image = image
        }
        this.identityMapper.identityRepository.save(identity)
        handleCache(identity)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    fun removeImage(identityId: UUID) {
        val identity = this.findEntity(identityId)
        val image = identity.image
        if (image != null) {
            identity.apply {
                this.image = null
            }
            this.identityMapper.identityRepository.save(identity)
            this.imageService.remove(image)
        } else {
            throw IllegalArgumentException("Identity image is null")
        }
        handleCache(identity)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    fun changePassword(identityRequest: IdentityRequest) {
        if (identityRequest.id == null) throw IllegalArgumentException("Identity id is null")
        if (identityRequest.password.isNullOrEmpty()) throw IllegalArgumentException("Identity password is null")

        val identity = this.findEntity(identityRequest.id)
        if (this.passwordEncoder.matches(identityRequest.password, identity.password)) {
            identity.apply {
                this.password = identityRequest.password
            }
            handleCache(identity)

        } else {
            throw IllegalArgumentException("Encoded and new password matches")
        }
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Exception::class, Throwable::class, RuntimeException::class]
    )
    fun changeStatus(identityRequest: IdentityRequest) {
        if (identityRequest.id == null) throw IllegalArgumentException("Identity id is null")
        if (identityRequest.enabled == null) throw IllegalArgumentException("Identity status is null")

        val identity = this.findEntity(identityRequest.id)
        if (identityRequest.enabled == identity.enabled) {
            identity.apply {
                this.enabled = identityRequest.enabled
            }
            handleCache(identity)

        } else {
            throw IllegalArgumentException("Request and identity statuses matched")
        }
    }
}