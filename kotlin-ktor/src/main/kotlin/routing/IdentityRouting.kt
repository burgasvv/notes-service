package org.burgas.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import org.burgas.database.DatabaseFactory
import org.burgas.database.IdentityEntity
import org.burgas.database.IdentityRequest
import org.burgas.service.IdentityService
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

fun Application.configureIdentityRouting() {

    val identityService = IdentityService()
    val paramUrlList: List<String> = listOf(
        "/api/v1/identities/by-id",
        "/api/v1/identities/delete",
        "/api/v1/identities/upload-image",
        "/api/v1/identities/remove-image"
    )
    val bodyUrlList: List<String> = listOf(
        "/api/v1/identities/update",
        "/api/v1/identities/change-password"
    )

    routing {

        @Suppress("DEPRECATION")
        intercept(ApplicationCallPipeline.Call) {

            if (paramUrlList.contains(call.request.path())) {

                val principal = call.principal<UserPasswordCredential>()
                    ?: throw IllegalArgumentException("Principal not found")
                val identityId = UUID.fromString(call.parameters["identityId"])

                val identityEntity = newSuspendedTransaction(
                    db = DatabaseFactory.postgres,
                    context = Dispatchers.Default,
                    readOnly = true
                ) {
                    IdentityEntity.findById(identityId)
                        ?: throw IllegalArgumentException("Identity not found for authentication")
                }
                if (identityEntity.email == principal.name) {
                    proceed()

                } else {
                    throw IllegalArgumentException("Identity not authorized")
                }

            } else if (bodyUrlList.contains(call.request.path())) {

                val principal = call.principal<UserPasswordCredential>()
                    ?: throw IllegalArgumentException("Principal not found")
                val identityRequest = call.receive<IdentityRequest>()
                val identityId = identityRequest.id
                    ?: throw IllegalArgumentException("Identity Request id is null for authentication")

                val identityEntity = newSuspendedTransaction(
                    db = DatabaseFactory.postgres,
                    context = Dispatchers.Default,
                    readOnly = true
                ) {
                    IdentityEntity.findById(identityId)
                        ?: throw IllegalArgumentException("Identity not found for authentication")
                }
                if (identityEntity.email == principal.name) {
                    call.attributes[AttributeKey<IdentityRequest>("identityRequest")] = identityRequest
                    proceed()

                } else {
                    throw IllegalArgumentException("Identity not authorized")
                }

            } else {
                proceed()
            }
        }

        route("/api/v1/identities") {

            post("/create") {
                val identityRequest = call.receive<IdentityRequest>()
                identityService.create(identityRequest)
                call.respond(HttpStatusCode.OK)
            }

            authenticate("basic-auth-admin") {

                get {
                    call.respond(HttpStatusCode.OK, identityService.findAll())
                }

                put("/change-status") {
                    val identityRequest = call.receive<IdentityRequest>()
                    identityService.changeStatus(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate("basic-auth-all") {

                get("/by-id") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    call.respond(HttpStatusCode.OK, identityService.findById(identityId))
                }

                put("/update") {
                    val identityRequest = call.attributes[AttributeKey<IdentityRequest>("identityRequest")]
                    identityService.update(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.delete(identityId)
                    call.respond(HttpStatusCode.OK)
                }

                put("/change-password") {
                    val identityRequest = call.attributes[AttributeKey<IdentityRequest>("identityRequest")]
                    identityService.changePassword(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }

                post("/upload-image") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.uploadImage(identityId, call.receiveMultipart())
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-image") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.removeImage(identityId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}