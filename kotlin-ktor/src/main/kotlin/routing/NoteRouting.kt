package org.burgas.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.burgas.database.NoteImage
import org.burgas.database.NoteRequest
import org.burgas.service.NoteService
import java.util.*

fun Application.configureNoteRouting() {

    val noteService = NoteService()

    routing {

        route("/api/v1/notes") {

            authenticate("basic-auth-all") {

                get {
                    call.respond(HttpStatusCode.OK, noteService.findAll())
                }

                get("/by-id") {
                    val noteId = UUID.fromString(call.parameters["noteId"])
                    call.respond(HttpStatusCode.OK, noteService.findById(noteId))
                }

                post("/create") {
                    val noteRequest = call.receive<NoteRequest>()
                    noteService.create(noteRequest)
                    call.respond(HttpStatusCode.OK)
                }

                put("/update") {
                    val noteRequest = call.receive<NoteRequest>()
                    noteService.update(noteRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val noteId = UUID.fromString(call.parameters["noteId"])
                    noteService.delete(noteId)
                    call.respond(HttpStatusCode.OK)
                }

                post("/upload-images") {
                    val noteId = UUID.fromString(call.parameters["noteId"])
                    noteService.uploadImages(noteId, call.receiveMultipart())
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-images") {
                    val noteId = UUID.fromString(call.parameters["noteId"])
                    val imageIds = call.receive<NoteImage>().imageIds
                    noteService.removeImages(noteId, imageIds)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}