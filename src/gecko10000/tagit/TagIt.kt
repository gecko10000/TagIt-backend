package gecko10000.tagit

import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import gecko10000.tagit.routing.fileRouting
import gecko10000.tagit.routing.tagRouting
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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
        }
    }.start(wait = true)
}
