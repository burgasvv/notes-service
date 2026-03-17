package org.burgas.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.burgas.database.NoteImage
import org.burgas.database.NoteRequest
import org.burgas.serialization.UUIDSerializer
import org.burgas.service.NoteService
import java.util.UUID

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