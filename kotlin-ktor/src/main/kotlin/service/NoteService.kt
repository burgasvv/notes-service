package org.burgas.service

import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.burgas.database.IdentityEntity
import org.burgas.database.NoteEntity
import org.burgas.database.NoteFullResponse
import org.burgas.database.NoteRequest
import org.burgas.database.NoteShortResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun NoteEntity.insert(noteRequest: NoteRequest) {
    this.title = noteRequest.title ?: throw IllegalArgumentException("Note title is null")
    this.content = noteRequest.content ?: throw IllegalArgumentException("Note content is null")
    this.createdAt = LocalDateTime.now().toKotlinLocalDateTime()
    this.identity = IdentityEntity.findById(noteRequest.identityId ?: throw IllegalArgumentException("Note identityId is null"))
            ?: throw IllegalArgumentException("Note identity not found")
}

fun NoteEntity.update(noteRequest: NoteRequest) {
    this.title = noteRequest.title ?: this.title
    this.content = noteRequest.content ?: this.content
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