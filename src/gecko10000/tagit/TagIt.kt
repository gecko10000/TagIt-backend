package gecko10000.tagit

import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import gecko10000.tagit.routing.fileRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

val savedFiles = ConcurrentHashMap<String, SavedFile>()
val tags = ConcurrentHashMap<String, Tag>()
val fileManager = FileManager()

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 10000) {
        routing {
            fileRouting()
        }
        install(ContentNegotiation) {
            json(Json)
        }
    }.start(wait = true)
}
