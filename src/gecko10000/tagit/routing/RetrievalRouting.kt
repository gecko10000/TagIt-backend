package gecko10000.tagit.routing

import gecko10000.tagit.fileController
import gecko10000.tagit.json.mapper.Mapper
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.tagController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

enum class Order(val comparator: Comparator<SavedFile>) {
    ALPHABETICAL(compareBy { it.file.name }),
    DATE_MODIFIED(compareBy { it.file.lastModified() }),
    SIZE(compareBy { it.file.length() }),
}

private fun Route.allTagsRoute() {
    get("all") {
        val tags = tagController.readOnlyTagMap().values.map { Mapper.CHILD_TAG.apply(it) }
        call.respondJson(mapOf("tags" to tags))
    }
}

private fun Route.allFilesRoute() {
    get("all") {
        val headers = call.request.headers
        val order = try {
            headers["order"]?.uppercase(Locale.getDefault())?.let { Order.valueOf(it) } ?: Order.DATE_MODIFIED
        } catch (ex: IllegalArgumentException) {
            return@get call.respond(HttpStatusCode.NotImplemented, "Ordering not found.")
        }
        val reversed = headers["reversed"] == "true" // false if not provided or something other than "true"
        var sortedFiles = fileController.readOnlyFileMap().values.sortedWith(order.comparator)
        if (reversed) sortedFiles = sortedFiles.reversed()
        call.respondJson(sortedFiles.map { Mapper.SAVED_FILE.apply(it) })
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
