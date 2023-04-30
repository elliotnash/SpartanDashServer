package org.chsrobotics.dash

import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.chsrobotics.dash.SpartanDash.json
import org.chsrobotics.dash.SpartanDash.widgetModels
import org.chsrobotics.dash.model.DashEvent
import org.chsrobotics.dash.model.WidgetEvent
import org.chsrobotics.dash.model.WidgetModel
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.LinkedHashSet
import kotlin.time.Duration.Companion.milliseconds

data class ValueUpdater<T>(
    var current: T,
    var last: T
) {
    constructor(value: T) : this(value, value)
    fun ifUpdated(cb: (next: T, last: T) -> Unit) {
        if (current != last) {
            cb(current, last)
            last = current
        }
    }
}

private class Connection(val session: DefaultWebSocketSession) {
    companion object {
        val lastId = AtomicInteger(0)
    }
    val name = "user${lastId.getAndIncrement()}"
}

private class DashServer {
    init {
        embeddedServer(Netty, port = 5810, module = {
            // Ktor wide json conversion
            install(ContentNegotiation) {
                json(json)
            }
            // WebSocket support
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
            // Configure routes
            routing { spartanRoutes() }
        }).start(wait = false)
    }

    private val connections: MutableSet<Connection> = Collections.synchronizedSet(LinkedHashSet())

    private fun Routing.spartanRoutes() {
        get("/ping") {
            call.respondText { "Pong!" }
        }
        webSocket("/ws") {
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                for (widget in widgetModels) {
                    sendSerialized(WidgetEvent(widget.value.current))
                }
                send("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    val test = receiveDeserialized<DashEvent>()
                    println(test)
//                    frame as? Frame.Text ?: continue
//                    val receivedText = frame.readText()
//                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
//                    connections.forEach {
//                        it.session.send(textWithUsername)
//                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
object SpartanDash {
    internal val json = Json {
        ignoreUnknownKeys = true
    }
    internal val widgetModels = mutableMapOf<String, ValueUpdater<WidgetModel>>()

    private val server: DashServer = DashServer()

    init {
        println("WE Init")
        GlobalScope.launch {
            eventLoop()
        }
    }

    private suspend fun eventLoop() {
        while (true) {
            for (updater in widgetModels.values) {
                updater.ifUpdated { next, _ ->
                    println("WE UPDATED: ${json.encodeToString(next)}")
                }
            }
            delay(1000.milliseconds)
        }
    }
}
