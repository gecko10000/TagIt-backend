package gecko10000.tagit.routing

import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.savedFiles
import gecko10000.tagit.tags
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.util.*

enum class Order(val comparator: Comparator<SavedFile>) {
    ALPHABETICAL(compareBy { it.file.name }),
    DATE_MODIFIED(compareBy { it.file.lastModified() }),
    SIZE(compareBy { it.file.length() }),
}

private suspend fun PipelineContext<Unit, ApplicationCall>.allFiles() {
    val headers = call.request.headers
    val order = try {
        headers["order"]?.uppercase(Locale.getDefault())?.let { Order.valueOf(it) } ?: Order.DATE_MODIFIED
    } catch (ex: IllegalArgumentException) {
        return call.respond(HttpStatusCode.NotImplemented, "Ordering not found.")
    }
    val reversed = headers["reversed"] == "true" // false if not provided or something other than "true"
    var sortedFiles = savedFiles.values.sortedWith(order.comparator)
    if (reversed) sortedFiles = sortedFiles.reversed()
    call.respondJson(sortedFiles)
}

fun Route.retrievalRouting() {
    route("/tags") {
        get("all") {
            call.respondJson(tags.values.sortedBy { it.name })
        }
    }
    route("/files") {
        get("all") {
            allFiles()
        }
    }
}
