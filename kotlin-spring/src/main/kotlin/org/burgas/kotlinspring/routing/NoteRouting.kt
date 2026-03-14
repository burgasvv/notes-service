package org.burgas.kotlinspring.routing

import org.burgas.kotlinspring.entity.exception.ExceptionResponse
import org.burgas.kotlinspring.entity.identity.IdentityDetails
import org.burgas.kotlinspring.entity.note.NoteRequest
import org.burgas.kotlinspring.service.NoteService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.servlet.function.body
import org.springframework.web.servlet.function.router
import java.util.*

@Configuration
class NoteRouting {

    private final val noteService: NoteService

    constructor(noteService: NoteService) {
        this.noteService = noteService
    }

    @Bean
    fun noteRouter() = router {

        "/api/v1/notes".nest {

            filter { request, function ->
                if (
                    request.path().equals("/api/v1/notes/by-id", false) ||
                    request.path().equals("/api/v1/notes/delete", false) ||
                    request.path().equals("/api/v1/notes/add-images", false) ||
                    request.path().equals("/api/v1/notes/remove-images", false)
                ) {
                    val authentication = request.principal()
                        .orElseThrow { throw IllegalArgumentException("Principal is null") } as Authentication

                    if (authentication.isAuthenticated) {
                        val identityDetails = authentication.principal as IdentityDetails
                        val noteId = UUID.fromString(request.param("noteId").orElseThrow())
                        val note = noteService.findEntity(noteId)

                        if (note.identity.id == identityDetails.identity.id) {
                            function(request)
                        } else {
                            throw IllegalArgumentException("Identity not authorized")
                        }

                    } else {
                        throw IllegalArgumentException("Identity not authenticated")
                    }

                } else if (request.path().equals("/api/v1/notes/create", false)) {
                    val authentication = request.principal()
                        .orElseThrow { throw IllegalArgumentException("Principal is null") } as Authentication

                    if (authentication.isAuthenticated) {
                        val identityDetails = authentication.principal as IdentityDetails
                        val noteRequest = request.body<NoteRequest>()
                        val identityId = noteRequest.identityId
                            ?: throw IllegalArgumentException("Note identityId is null for authentication")

                        if (identityDetails.identity.id == identityId) {
                            request.attributes()["noteRequest"] = noteRequest
                            function(request)

                        } else {
                            throw IllegalArgumentException("Identity not authorized")
                        }

                    } else {
                        throw IllegalArgumentException("Identity not authenticated")
                    }

                } else if (request.path().equals("/api/v1/notes/update", false)) {
                    val authentication = request.principal()
                        .orElseThrow { throw IllegalArgumentException("Principal is null") } as Authentication

                    if (authentication.isAuthenticated) {
                        val identityDetails = authentication.principal as IdentityDetails
                        val noteRequest = request.body<NoteRequest>()
                        val noteId =
                            noteRequest.id ?: throw IllegalArgumentException("Note Request id is null for authentication")
                        val note = noteService.findEntity(noteId)

                        if (note.identity.id == identityDetails.identity.id) {
                            request.attributes()["noteRequest"] = noteRequest
                            function(request)

                        } else {
                            throw IllegalArgumentException("Identity not authorized")
                        }

                    } else {
                        throw IllegalArgumentException("Identity not authenticated")
                    }

                } else {
                    function(request)
                }
            }

            GET("") {
                ok().body(noteService.findAll())
            }

            GET("/by-id") {
                val noteId = UUID.fromString(it.param("noteId").orElseThrow())
                ok().body(noteService.findById(noteId))
            }

            POST("/create") {
                val noteRequest = it.attributes()["noteRequest"] as NoteRequest
                println(noteRequest)
                noteService.create(noteRequest)
                ok().build()
            }

            PUT("/update") {
                val noteRequest = it.attributes()["noteRequest"] as NoteRequest
                noteService.update(noteRequest)
                ok().build()
            }

            DELETE("/delete") {
                val noteId = UUID.fromString(it.param("noteId").orElseThrow())
                noteService.delete(noteId)
                ok().build()
            }

            POST("/add-images") {
                val noteId = UUID.fromString(it.param("noteId").orElseThrow())
                val parts = it.multipartData()["image"]!!
                noteService.addImages(noteId, parts)
                ok().build()
            }

            DELETE("/remove-images") {
                val imageIds = it.body<List<UUID>>()
                val noteId = UUID.fromString(it.param("noteId").orElseThrow())
                noteService.removeImages(noteId, imageIds)
                ok().build()
            }

            onError({ true }) { throwable, _ ->
                val exception = ExceptionResponse(
                    status = HttpStatus.BAD_REQUEST.name,
                    code = HttpStatus.BAD_REQUEST.ordinal,
                    message = throwable.localizedMessage
                )
                ok().body(exception)
            }
        }
    }
}