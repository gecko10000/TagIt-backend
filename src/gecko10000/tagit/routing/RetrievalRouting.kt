package gecko10000.tagit.routing

import gecko10000.tagit.fileController
import gecko10000.tagit.json.mapper.JsonMapper
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.tagController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

enum class Order(val comparator: Comparator<SavedFile>) {
    FILE_NAME(compareBy { it.file.name }),
    MODIFICATION_DATE(compareBy { it.file.lastModified() }),
    FILE_SIZE(compareBy { it.file.length() }),
    NUM_TAGS(compareBy { it.tags.size }),
    FILE_TYPE(compareBy { it.file.extension })
}

private fun Route.allTagsRoute() {
    get("all") {
        val tags = tagController.readOnlyTagMap().values.map { JsonMapper.CHILD_TAG.apply(it) }
        call.respondJson(mapOf("tags" to tags))
    }
}

private fun Route.allFilesRoute() {
    get("all") {
        val headers = call.request.headers
        val order = try {
            headers["order"]?.uppercase(Locale.getDefault())?.let { Order.valueOf(it) } ?: Order.MODIFICATION_DATE
        } catch (ex: IllegalArgumentException) {
            return@get call.respond(HttpStatusCode.NotImplemented, "Ordering not found.")
        }
        val reversed = headers["reversed"] == "true" // false if not provided or something other than "true"
        var sortedFiles = fileController.readOnlyFileMap().values.sortedWith(order.comparator)
        if (reversed) sortedFiles = sortedFiles.reversed()
        call.respondJson(sortedFiles.map { JsonMapper.SAVED_FILE.apply(it) })
    }
}

fun Route.retrievalRouting() {
    route("/tags") {
        allTagsRoute()
    }
    route("/files") {
        allFilesRoute()
    }
}
