package gecko10000.tagit.routing

import gecko10000.tagit.fileController
import gecko10000.tagit.json.mapper.Mapper
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.tagController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private suspend fun ensureFileExists(call: ApplicationCall): SavedFile? {
    val name = call.parameters["name"]
    val existing = fileController[name]
    existing ?: call.respond(HttpStatusCode.NotFound, "File not found.")
    return existing
}

private fun Route.getFileRoute() {
    get("{name}") {
        val savedFile = ensureFileExists(call) ?: return@get
        call.respondFile(savedFile.file)
    }
}


private fun Route.getFileInfoRoute() {
    get("{name}/info") {
        val savedFile = ensureFileExists(call) ?: return@get
        call.respondJson(Mapper.SAVED_FILE.apply(savedFile))
    }
}

private fun Route.uploadFileRoute() {
    post("{name}") {
        val name = call.parameters["name"]!!
        if (name.contains('/')) return@post call.respond(HttpStatusCode.BadRequest, "Slashes not allowed in filename.")
        val existing = fileController[name]
        existing?.run { return@post call.respond(HttpStatusCode.BadRequest, "File already exists.") }
        val stream = call.receiveStream()
        withContext(Dispatchers.IO) {
            fileController.addFile(stream, name, call)
            stream.close()
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.renameFileRoute() {
    patch("{name}") {
        val existing = ensureFileExists(call) ?: return@patch
        val params = call.receiveParameters()
        val newName = params["name"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "No new name sent.")
        if (newName.contains('/')) return@patch call.respond(
            HttpStatusCode.BadRequest,
            "Slashes not allowed in filename."
        )
        fileController.renameFile(existing, newName, call)
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.addTagRoute() {
    patch("{name}/add") {
        val existing = ensureFileExists(call) ?: return@patch
        val params = call.receiveParameters()
        val tagName = params["tag"]?.trimEnd('/')
        val tag = tagName?.let { tagController[it] ?: tagController.createTag(it) }
        tag ?: return@patch call.respond(HttpStatusCode.BadRequest, "No valid tags sent.")
        fileController.addTag(existing, tag)
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.removeTagRoute() {
    patch("{name}/remove") {
        val existing = ensureFileExists(call) ?: return@patch
        val params = call.receiveParameters()
        val tagName = params["tag"]?.trimEnd('/')
        val tag = tagController[tagName]
        tag ?: return@patch call.respond(HttpStatusCode.BadRequest, "No valid tags sent.")
        // remove tags from file
        fileController.removeTag(existing, tag)
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.deleteFileRoute() {
    delete("{name}") {
        val existing = ensureFileExists(call) ?: return@delete
        fileController.deleteFile(existing)
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.fileRouting() {
    route("/file") {
        getFileRoute()
        getFileInfoRoute()
        uploadFileRoute()
        renameFileRoute()
        addTagRoute()
        removeTagRoute()
        deleteFileRoute()
    }
}
