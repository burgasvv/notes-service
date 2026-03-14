package org.burgas.kotlinspring.mapper

import org.burgas.kotlinspring.entity.identity.Authority
import org.burgas.kotlinspring.entity.identity.Identity
import org.burgas.kotlinspring.entity.identity.IdentityFullResponse
import org.burgas.kotlinspring.entity.identity.IdentityRequest
import org.burgas.kotlinspring.entity.identity.IdentityShortResponse
import org.burgas.kotlinspring.repository.IdentityRepository
import org.springframework.beans.factory.ObjectFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class IdentityMapper : Mapper<IdentityRequest, Identity, IdentityShortResponse, IdentityFullResponse> {

    final val identityRepository: IdentityRepository
    private final val noteMapperObjectFactory: ObjectFactory<NoteMapper>
    private final val passwordEncoder: PasswordEncoder

    constructor(
        identityRepository: IdentityRepository,
        noteMapperObjectFactory: ObjectFactory<NoteMapper>,
        passwordEncoder: PasswordEncoder
    ) {
        this.identityRepository = identityRepository
        this.noteMapperObjectFactory = noteMapperObjectFactory
        this.passwordEncoder = passwordEncoder
    }

    private fun getNoteMapper(): NoteMapper = this.noteMapperObjectFactory.`object`

    override fun toEntity(request: IdentityRequest): Identity {
        return this.identityRepository.findById(request.id ?: UUID(0, 0))
            .map {
                Identity().apply {
                    this.id = it.id
                    this.authority = request.authority ?: it.authority
                    this.username = request.username ?: it.username
                    this.password = it.password
                    this.email = request.email ?: it.email
                    this.enabled = request.enabled ?: it.enabled
                    this.firstname = request.firstname ?: it.firstname
                    this.lastname = request.lastname ?: it.lastname
                    this.patronymic = request.patronymic ?: it.patronymic
                    this.image = it.image
                }
            }
            .orElseGet {
                Identity().apply {
                    val newPassword =
                        if (!request.password.isNullOrEmpty()) passwordEncoder.encode(request.password) else throw IllegalArgumentException(
                            "Identity password is null or empty"
                        )
                    this.authority = request.authority ?: Authority.USER
                    this.username = request.username ?: throw IllegalArgumentException("Identity username is null")
                    this.password = newPassword ?: throw IllegalArgumentException("Password not encoded")
                    this.email = request.email ?: throw IllegalArgumentException("Identity email is null")
                    this.enabled = request.enabled ?: true
                    this.firstname = request.firstname ?: throw IllegalArgumentException("Identity firstname is null")
                    this.lastname = request.lastname ?: throw IllegalArgumentException("Identity lastname is null")
                    this.patronymic =
                        request.patronymic ?: throw IllegalArgumentException("Identity patronymic is null")
                }
            }
    }

    override fun toShortResponse(entity: Identity): IdentityShortResponse {
        return IdentityShortResponse(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            firstname = entity.firstname,
            lastname = entity.lastname,
            patronymic = entity.patronymic,
            image = entity.image
        )
    }

    override fun toFullResponse(entity: Identity): IdentityFullResponse {
        return IdentityFullResponse(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            firstname = entity.firstname,
            lastname = entity.lastname,
            patronymic = entity.patronymic,
            image = entity.image,
            notes = entity.notes.map { this.getNoteMapper().toShortResponse(it) }
        )
    }
}