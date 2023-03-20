package gecko10000.tagit.routing

import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import gecko10000.tagit.savedFiles
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

suspend fun PipelineContext<Unit, ApplicationCall>.get() {
    val savedFile = savedFiles[call.parameters["name"]]
    savedFile ?: return@get call.respond(HttpStatusCode.NotFound)
    call.respondFile(savedFile.file)
}

suspend fun PipelineContext<Unit, ApplicationCall>.post() {
    val name = call.parameters["name"]!!
    val existing = savedFiles[name]
    existing?.run { return@post call.respond(HttpStatusCode.Forbidden) }
    val stream = call.receiveStream()
    val file = File("files/$name")
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

fun Route.fileRouting() {
    route("/file") {
        get("{name}") {
            get()
        }
        post("{name}") {
            post()
        }
    }
}
