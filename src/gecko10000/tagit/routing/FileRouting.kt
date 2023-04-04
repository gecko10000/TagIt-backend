package gecko10000.tagit.routing

import gecko10000.tagit.fileManager
import gecko10000.tagit.misc.fileDirectory
import gecko10000.tagit.misc.respondJson
import gecko10000.tagit.objects.SavedFile
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
    // TODO: check filesystem?
    existing ?: call.respond(HttpStatusCode.NotFound, "File not found.")
    return existing
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getFile() {
    val savedFile = ensureFileExists(call) ?: return
    call.respondFile(savedFile.file)
}



private suspend fun PipelineContext<Unit, ApplicationCall>.getTags() {
    val savedFile = ensureFileExists(call) ?: return
    //println(Json.encodeToString(savedFile.tags))
    call.respondJson(savedFile.tags)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.postFile() {
    val name = call.parameters["name"]!!
    val existing = savedFiles[name]
    existing?.run { return@postFile call.respond(HttpStatusCode.Forbidden, "File already exists.") }
    val stream = call.receiveStream()
    val file = File("$fileDirectory$name")
    savedFiles[name] = SavedFile(file)
    withContext(Dispatchers.IO) {
        try {
            stream.transferTo(file.outputStream())
        } catch (ex: IOException) {
            call.respond(HttpStatusCode.InternalServerError, ex)
            savedFiles.remove(name)
            return@withContext
        }
        call.respond(HttpStatusCode.OK)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.patchAddTags() {
    val existing = ensureFileExists(call) ?: return
    val params = call.receiveParameters()
    val sentTags = params["tags"]?.run { Json.decodeFromString<Array<String>>(this) }?.mapNotNull { tags[it] }
    if (sentTags.isNullOrEmpty()) {
        call.respond(HttpStatusCode.BadRequest, "No valid tags sent.")
        return
    }
    // add tags to file
    fileManager.addTags(existing, *sentTags.toTypedArray())
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.patchRemoveTags() {
    val existing = ensureFileExists(call) ?: return
    val params = call.receiveParameters()
    val sentTags = params["tags"]?.run { Json.decodeFromString<Array<String>>(this) }?.mapNotNull { tags[it] }
    if (sentTags.isNullOrEmpty()) {
        call.respond(HttpStatusCode.BadRequest, "No valid tags sent.")
        return
    }
    // remove tags from file
    fileManager.removeTags(existing, *sentTags.toTypedArray())
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.deleteFile() {
    val existing = ensureFileExists(call) ?: return
    fileManager.removeTags(existing, *existing.tags.toTypedArray())
    existing.file.delete()
    call.respond(HttpStatusCode.OK)
}

fun Route.fileRouting() {
    route("/file") {
        get("{name}") {
            this.getFile()
        }
        get("{name}/tags") {
            this.getTags()
        }
        post("{name}") {
            this.postFile()
        }
        patch("{name}/add") {
            this.patchAddTags()
        }
        patch("{name}/remove") {
            this.patchRemoveTags()
        }
        delete("{name}") {
            this.deleteFile()
        }
    }
}
