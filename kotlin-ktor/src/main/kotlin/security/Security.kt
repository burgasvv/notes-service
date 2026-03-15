package org.burgas.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.csrf.CSRF
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import kotlinx.serialization.Serializable
import org.burgas.serialization.UUIDSerializer
import java.util.UUID

@Serializable
data class ExceptionResponse(
    val status: String,
    val code: Int,
    val message: String
)

@Serializable
data class CsrfToken(@Serializable(with = UUIDSerializer::class) val token: UUID)

fun Application.configureSecurity() {

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val exceptionResponse = ExceptionResponse(
                status = HttpStatusCode.BadGateway.description,
                code = HttpStatusCode.BadGateway.value,
                message = cause.localizedMessage
            )
            call.respond(exceptionResponse)
        }
    }

    install(Sessions) {
        cookie<CsrfToken>("CSRF_TOKEN")
    }

    install(CSRF) {
        allowOrigin("http://localhost:9010")
        originMatchesHost()
        checkHeader("X-CSRF-Token")
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)

        allowHeader("X-CSRF-Token")
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Host)

        anyHost()
    }
}