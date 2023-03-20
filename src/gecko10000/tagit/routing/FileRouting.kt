package gecko10000.tagit.routing

import gecko10000.tagit.fileDirectory
import gecko10000.tagit.fileManager
import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import gecko10000.tagit.savedFiles
import gecko10000.tagit.tags
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

private suspend fun ensureFileExists(call: ApplicationCall): SavedFile? {
    val name = call.parameters["name"]
    val existing = savedFiles[name]
    existing ?: call.respond(HttpStatusCode.NotFound)
    return existing
}

suspend fun PipelineContext<Unit, ApplicationCall>.get() {
    val savedFile = ensureFileExists(call) ?: return
    call.respondFile(savedFile.file)
}

suspend fun PipelineContext<Unit, ApplicationCall>.post() {
    val name = call.parameters["name"]!!
    val existing = savedFiles[name]
    existing?.run { return@post call.respond(HttpStatusCode.Forbidden) }
    val stream = call.receiveStream()
    val file = File("$fileDirectory$name")
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

suspend fun PipelineContext<Unit, ApplicationCall>.patchAdd() {
    val existing = ensureFileExists(call) ?: return
    val params = call.receiveParameters()
    val sentTags = params["tags"]?.run { Json.decodeFromString<Array<String>>(this) }?.mapNotNull { tags[it] }
    if (sentTags.isNullOrEmpty()) {
        call.respond(HttpStatusCode.BadRequest)
        return
    }
    // add tags to file
    fileManager.addTags(existing, *sentTags.toTypedArray())
    call.respond(HttpStatusCode.OK)
}

suspend fun PipelineContext<Unit, ApplicationCall>.patchRemove() {
    val existing = ensureFileExists(call) ?: return
    val params = call.receiveParameters()
    val sentTags = params["tags"]?.run { Json.decodeFromString<Array<String>>(this) }?.mapNotNull { tags[it] }
    if (sentTags.isNullOrEmpty()) {
        call.respond(HttpStatusCode.BadRequest)
        return
    }
    // remove tags from file
    fileManager.removeTags(existing, *sentTags.toTypedArray())
    call.respond(HttpStatusCode.OK)
}

fun Route.fileRouting() {
    route("/file") {
        get("{name}") {
            get()
        }
        post("{name}") {
            post()
        }
        patch("{name}/add") {
            patchAdd()
        }
        patch("{name}/remove") {
            patchRemove()
        }
    }
}
