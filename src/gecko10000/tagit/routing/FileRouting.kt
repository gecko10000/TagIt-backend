package gecko10000.tagit.routing

import gecko10000.tagit.*
import gecko10000.tagit.json.mapper.JsonMapper
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.misc.extension.uuidFromStringSafe
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private suspend fun ensureFileExists(call: ApplicationCall): SavedFile? {
    val uuid = uuidFromStringSafe(call.parameters["uuid"])
    uuid ?: run {
        call.respond(HttpStatusCode.BadRequest, "Invalid UUID parameter.")
        return null
    }
    val existing = fileController[uuid]
    existing ?: call.respond(HttpStatusCode.NotFound, "File not found.")
    return existing
}

// note: this route is special because we cannot use headers for videos/audio in browser.
// therefore, the token is provided as a query parameter.
private fun Route.getFileRoute() {
    get("{uuid}") {
        val token = call.request.queryParameters["token"]
        token?.let { db.userFromToken(token) } ?: return@get call.respond(HttpStatusCode.Unauthorized)
        val savedFile = ensureFileExists(call) ?: return@get
        call.respondFile(savedFile.file)
    }
}


private fun Route.getFileInfoRoute() {
    get("{uuid}/info") {
        val savedFile = ensureFileExists(call) ?: return@get
        call.respondJson(JsonMapper.SAVED_FILE.apply(savedFile))
    }
}

private fun Route.getFileThumbnailRoute() {
    get("{uuid}/thumb") {
        val savedFile = ensureFileExists(call) ?: return@get
        thumbnailController.getThumbnail(savedFile)?.let { call.respondFile(it) }
            ?: call.respond(HttpStatusCode.NotFound)
    }
}

private fun Route.uploadFileRoute() {
    post("{name}") {
        val name = call.parameters["name"]!!
        if (name.contains('/')) return@post call.respond(HttpStatusCode.BadRequest, "Slashes not allowed in filename.")
        val existing = fileController.readOnlyFileMap().values.firstOrNull { it.file.name == name }
        existing?.run { return@post call.respond(HttpStatusCode.Forbidden, "File already exists.") }
        val stream = call.receiveStream()
        withContext(Dispatchers.IO) {
            val savedFile = fileController.addFile(stream, name, call)
            stream.close()
            if (savedFile == null) {
                return@withContext call.respond(HttpStatusCode.InternalServerError, "Could not save file.")
            }
            call.respondJson(JsonMapper.SAVED_FILE.apply(savedFile))
        }
    }
}

private fun Route.renameFileRoute() {
    patch("{uuid}") {
        mutex.withLock {
            val existing = ensureFileExists(call) ?: return@patch
            val params = call.receiveParameters()
            val newName = params["name"] ?: return@patch call.respond(HttpStatusCode.BadRequest, "No new name sent.")
            if (newName.contains('/')) return@patch call.respond(
                HttpStatusCode.BadRequest,
                "Slashes not allowed in filename."
            )
            fileController.renameFile(existing, newName, call)
        }
        call.respond(HttpStatusCode.OK)
    }
}

private suspend fun getTagFromParams(call: ApplicationCall): Tag? {
    val params = call.receiveParameters()
    val tagIdString = params["tag"] ?: run {
        call.respond(HttpStatusCode.BadRequest, "No tag UUID sent.")
        return null
    }
    val tagId = uuidFromStringSafe(tagIdString) ?: run {
        call.respond(HttpStatusCode.BadRequest, "Invalid tag UUID sent.")
        return null
    }
    val tag = tagController[tagId] ?: run {
        call.respond(HttpStatusCode.BadRequest, "UUID does not specify a tag.")
        return null
    }
    return tag
}

private fun Route.addTagRoute() {
    patch("{uuid}/add") {
        mutex.withLock {
            val existing = ensureFileExists(call) ?: return@patch
            val tag = getTagFromParams(call) ?: return@patch
            fileController.addTag(existing, tag)
        }
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.removeTagRoute() {
    patch("{uuid}/remove") {
        mutex.withLock {
            val existing = ensureFileExists(call) ?: return@patch
            val tag = getTagFromParams(call) ?: return@patch
            fileController.removeTag(existing, tag)
        }
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.deleteFileRoute() {
    delete("{uuid}") {
        mutex.withLock {
            val existing = ensureFileExists(call) ?: return@delete
            fileController.deleteFile(existing)
        }
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.fileRouting() {
    route("/file") {
        authenticate("auth-bearer") {
            getFileInfoRoute()
            getFileThumbnailRoute()
            uploadFileRoute()
            renameFileRoute()
            addTagRoute()
            removeTagRoute()
            deleteFileRoute()
        }
        getFileRoute()
    }
}
