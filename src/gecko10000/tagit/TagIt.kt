package gecko10000.tagit

import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import gecko10000.tagit.routing.fileRouting
import gecko10000.tagit.routing.retrievalRouting
import gecko10000.tagit.routing.searchRouting
import gecko10000.tagit.routing.tagRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import java.util.concurrent.ConcurrentHashMap

val savedFiles = ConcurrentHashMap<String, SavedFile>()
val tags = ConcurrentHashMap<String, Tag>()
val fileManager = FileManager()

fun main() {
    embeddedServer(Netty, port = 10000) {
        routing {
            fileRouting()
            tagRouting()
            retrievalRouting()
            searchRouting()
        }
        install(CORS) {
            anyHost()
            allowHeaders { true }
            allowMethod(HttpMethod.Patch)
            allowMethod(HttpMethod.Delete)
        }
    }.start(wait = true)
}
