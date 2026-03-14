package org.burgas.kotlinspring.routing

import org.burgas.kotlinspring.entity.note.NoteRequest
import org.burgas.kotlinspring.service.NoteService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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

            GET("") {
                ok().body(noteService.findAll())
            }

            GET("/by-id") {
                val noteId = UUID.fromString(it.param("noteId").orElseThrow())
                ok().body(noteService.findById(noteId))
            }

            POST("/create") {
                val noteRequest = it.body<NoteRequest>()
                noteService.create(noteRequest)
                ok().build()
            }

            PUT("/update") {
                val noteRequest = it.body<NoteRequest>()
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
        }
    }
}