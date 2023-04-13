package gecko10000.tagit.routing

import gecko10000.tagit.misc.respondJson
import gecko10000.tagit.savedFiles
import gecko10000.tagit.tags
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.retrievalRouting() {
    route("tags") {
        get("all") {
            call.respondJson(tags.values.sortedBy { it.name })
        }
        get("search") {
            call.respond(HttpStatusCode.NotImplemented)
        }
    }
    route("files") {
        get("all") {
            call.respondJson(savedFiles.values.sortedBy { it.file.name })
        }
        get("search") {
            call.respond(HttpStatusCode.NotImplemented)
        }
    }
}
