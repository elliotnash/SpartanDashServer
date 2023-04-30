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
import org.chsrobotics.dash.layout.DashLayout
import org.chsrobotics.dash.model.DashEvent
import org.chsrobotics.dash.model.DashLayoutEvent
import org.chsrobotics.dash.model.WidgetEvent
import org.chsrobotics.dash.model.WidgetModel
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Supplier
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

private class Connection(val session: DefaultWebSocketServerSession) {
    companion object {
        val lastId = AtomicInteger(0)
    }
    val id = lastId.getAndIncrement()

    override fun equals(other: Any?): Boolean {
        if (other !is Connection) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = session.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}

private class DashServer {
    init {
        embeddedServer(Netty, port = 5810, module = {
            // Ktor wide json conversion
            install(ContentNegotiation) {
                json(SpartanDash.json)
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
                send("You are connected! There are ${connections.count()} users here.")
                sendSerialized(SpartanDash.widgetModels.values.map { WidgetEvent(it.current)} as List<DashEvent>)

                while(true) {
                    val events = receiveDeserialized<List<DashEvent>>()
                    launch {
                        for (event in events) {
                            if (event is WidgetEvent) {
                                println("Received widget: ${event.widget.uuid}")
                                for (connection in connections) {
                                    // Update model
                                    SpartanDash.widgetModels[event.widget.uuid]?.current = event.widget
                                    SpartanDash.widgetModels[event.widget.uuid]?.last = event.widget

                                    if (connection != thisConnection) {
                                        connection.session.sendSerialized(listOf<DashEvent>(event))
                                    }
                                }
                            } else if (event is DashLayoutEvent) {
                                println("Received layout: ${event.layout}")
                                // Build layout for size and return
                                val layout = DashLayout(event.layout.columns, event.layout.rows)
                                SpartanDash.layoutBuilder?.let { it(layout) }
                                sendSerialized(layout.toDashLayoutModel())
                            }
                        }
                    }
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

typealias LayoutBuilder = DashLayout.() -> Unit

@OptIn(DelicateCoroutinesApi::class)
class SpartanDash(
    layout: LayoutBuilder,
) {
    // Interop constructor for java Consumer
    constructor(layout: Consumer<DashLayout>) : this({
        layout.accept(this)
    })
    init {
        layoutBuilder = layout
    }
    companion object {
        internal val json = Json {
            ignoreUnknownKeys = true
        }
        internal val widgetModels = mutableMapOf<String, ValueUpdater<WidgetModel>>()
        internal var layoutBuilder: LayoutBuilder? = null

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
}
