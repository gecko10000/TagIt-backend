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
    if (name.contains('/')) return call.respond(HttpStatusCode.Forbidden, "Slashes not allowed in filename.")
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

private suspend fun PipelineContext<Unit, ApplicationCall>.patchRenameFile() {
    val existing = ensureFileExists(call) ?: return
    val params = call.receiveParameters()
    val newName = params["name"] ?: return call.respond(HttpStatusCode.BadRequest, "No new name sent.")
    if (newName.contains('/')) return call.respond(HttpStatusCode.Forbidden, "Slashes not allowed in filename.")
    val newFile = existing.file.parentFile.resolve(newName)
    if (newFile.exists()) return call.respond(HttpStatusCode.Forbidden, "New filename already exists.")
    existing.file.renameTo(newFile)
    val tags = existing.tags.toTypedArray()
    fileManager.removeTags(existing, *tags)
    savedFiles.remove(call.parameters["name"])

    val newSavedFile = SavedFile(newFile)
    savedFiles[newName] = newSavedFile
    fileManager.addTags(newSavedFile, *tags)
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.patchAddTags() {
    val existing = ensureFileExists(call) ?: return
    val params = call.receiveParameters()
    val sentTagNames = params["tags"]?.let { Json.decodeFromString<Array<String>>(it) }?.map { it.trimEnd('/') }
    val sentTags = sentTagNames?.mapNotNull { tags[it] ?: fileManager.createTag(it) }
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
    val sentTags = params["tags"]?.run { Json.decodeFromString<Array<String>>(this) }?.mapNotNull { tags[it.trimEnd('/')] }
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
    savedFiles.remove(call.parameters["name"])
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
        patch("{name}") {
            this.patchRenameFile()
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
