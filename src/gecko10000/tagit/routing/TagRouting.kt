package gecko10000.tagit.routing

import gecko10000.tagit.fileManager
import gecko10000.tagit.misc.respondJson
import gecko10000.tagit.tags
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

private suspend fun PipelineContext<Unit, ApplicationCall>.getTag() {
    val tag = tags[call.parameters["name"]] ?: return call.respond(HttpStatusCode.BadRequest, "Tag not found.")
    call.respondJson(tag)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.postTag() {
    val name = call.parameters["name"]!!
    tags[name]?.run { return call.respond(HttpStatusCode.BadRequest, "Tag already exists.") }
    fileManager.createTag(name)
    call.respond(HttpStatusCode.OK)
}

fun Route.tagRouting() {
    route("/tag") {
        get("{name}") {
            this.getTag()
        }
        post("{name}") {
            this.postTag()
        }
    }
}
