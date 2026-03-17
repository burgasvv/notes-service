package org.burgas

import io.ktor.server.application.*
import org.burgas.database.configureDatabase
import org.burgas.routing.configureIdentityRouting
import org.burgas.routing.configureImageRouting
import org.burgas.routing.configureNoteRouting
import org.burgas.routing.configureSecurityRouting
import org.burgas.routing.configureWebsocketRouting
import org.burgas.security.configureSecurity
import org.burgas.serialization.configureSerialization
import org.burgas.websocket.configureWebsocket
import java.util.UUID

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureDatabase()
    configureWebsocket()

    configureSecurityRouting()
    configureWebsocketRouting()
    configureImageRouting()
    configureIdentityRouting()
    configureNoteRouting()
}