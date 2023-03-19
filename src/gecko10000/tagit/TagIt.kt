package gecko10000.tagit

import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

val savedFiles = ConcurrentHashMap<String, SavedFile>()
val tags = ConcurrentHashMap<String, Tag>()

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 10000) {
        routing {
            route("/file") {
                get("{name}") {
                    val savedFile = savedFiles[call.parameters["name"]]
                    savedFile ?: return@get call.respond(HttpStatusCode.NotFound)
                    call.respondFile(savedFile.file)
                }
                post("{name}") {
                    val name = call.parameters["name"]!!
                    val existing = savedFiles[name]
                    existing?.run { return@post call.respond(HttpStatusCode.Forbidden) }
                    val stream = call.receiveStream()
                    val file = File(name)
                    savedFiles[name] = SavedFile(file)
                    withContext(Dispatchers.IO) {
                        try {
                            stream.transferTo(file.outputStream())
                        } catch (ex: IOException) {
                            call.respond(HttpStatusCode.InternalServerError)
                            savedFiles.remove(name)
                            return@withContext
                        }
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }.start(wait = true)
}
