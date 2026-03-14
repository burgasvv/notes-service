package org.burgas.kotlinspring.mapper

import org.burgas.kotlinspring.entity.note.Note
import org.burgas.kotlinspring.entity.note.NoteFullResponse
import org.burgas.kotlinspring.entity.note.NoteRequest
import org.burgas.kotlinspring.entity.note.NoteShortResponse
import org.burgas.kotlinspring.repository.NoteRepository
import org.springframework.beans.factory.ObjectFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID

@Component
class NoteMapper : Mapper<NoteRequest, Note, NoteShortResponse, NoteFullResponse> {

    final val noteRepository: NoteRepository
    private final val identityMapperObjectFactory: ObjectFactory<IdentityMapper>

    constructor(noteRepository: NoteRepository, identityMapperObjectFactory: ObjectFactory<IdentityMapper>) {
        this.noteRepository = noteRepository
        this.identityMapperObjectFactory = identityMapperObjectFactory
    }

    private fun getIdentityMapper(): IdentityMapper = this.identityMapperObjectFactory.`object`

    override fun toEntity(request: NoteRequest): Note {
        return this.noteRepository.findById(request.id ?: UUID(0, 0))
            .map {
                Note().apply {
                    this.id = it.id
                    this.title = request.title ?: it.title
                    this.content = request.content ?: it.content
                    this.createdAt = it.createdAt
                    this.identity = getIdentityMapper().identityRepository.findById(request.identityId ?: UUID(0, 0))
                        .orElse(null) ?: it.identity
                }
            }
            .orElseGet {
                Note().apply {
                    this.title = request.title ?: throw IllegalArgumentException("Note title is null")
                    this.content = request.content ?: throw IllegalArgumentException("Note content is null")
                    this.createdAt = LocalDateTime.now()
                    this.identity = getIdentityMapper().identityRepository.findById(request.identityId ?: UUID(0, 0))
                        .orElse(null) ?: throw IllegalArgumentException("Note identity is null")
                }
            }
    }

    override fun toShortResponse(entity: Note): NoteShortResponse {
        return NoteShortResponse(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            createdAt = entity.createdAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
            images = entity.images
        )
    }

    override fun toFullResponse(entity: Note): NoteFullResponse {
        return NoteFullResponse(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            createdAt = entity.createdAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
            identity = Optional.ofNullable(entity.identity).map { this.getIdentityMapper().toShortResponse(it) }
                .orElse(null),
            images = entity.images
        )
    }
}