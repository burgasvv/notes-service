package org.burgas.routing

import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import java.util.Collections

fun Application.configureWebsocketRouting() {

    val connections = Collections.synchronizedSet<DefaultWebSocketServerSession>(LinkedHashSet())

    routing {

        route("/api/v1/chat") {

            webSocket {
                send("Вы подключены!")
                connections += this

                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {

                            val text = frame.readText()
                            if (text.equals("bye", ignoreCase = true)) {
                                close(CloseReason(CloseReason.Codes.NORMAL, "Клиент отключился"))

                            } else {
                                connections.forEach {
                                    it.send("Сервер получил сообщение: $text")
                                }
                            }
                        }
                    }
                } finally {
                    connections -= this
                }
            }
        }
    }
}