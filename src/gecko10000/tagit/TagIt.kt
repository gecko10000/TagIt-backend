package gecko10000.tagit

import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.concurrent.ConcurrentHashMap

val savedFiles = ConcurrentHashMap<String, SavedFile>()
val tags = ConcurrentHashMap<String, Tag>()

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 10000) {
        routing {
            route("/file") {
                get("{name?}") {
                    val savedFile = savedFiles[call.parameters["name"]]
                    savedFile ?: return@get call.respondText("404")
                    call.respondFile(savedFile.file)
                }
            }
        }
    }.start(wait = true)
}
