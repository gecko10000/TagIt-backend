package gecko10000.tagit.routing

import gecko10000.tagit.json.mapper.Mapper
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.model.Tag
import gecko10000.tagit.tagController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private fun getTagName(call: ApplicationCall) = call.parameters["name"]?.trimEnd('/')

private suspend fun ensureTagExists(call: ApplicationCall): Tag? {
    val name = getTagName(call)
    val existing = tagController[name]
    existing ?: call.respond(HttpStatusCode.NotFound, "Tag not found.")
    return existing
}

private fun Route.getTagRoute() {
    get("{name}") {
        val tag = ensureTagExists(call) ?: return@get
        call.respondJson(Mapper.TAG.apply(tag))
    }
}

private fun Route.createTagRoute() {
    post("{name}") {
        val name = getTagName(call)!!
        tagController[name]?.run { return@post call.respond(HttpStatusCode.BadRequest, "Tag already exists.") }
        tagController.createTag(name) ?: return@post call.respond(
            HttpStatusCode.InternalServerError,
            "Could not create tag."
        )
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.renameTagRoute() {

    patch("{name}") {
        val tag = ensureTagExists(call) ?: return@patch
        val params = call.receiveParameters()
        val newName =
            params["name"]?.trimEnd('/') ?: return@patch call.respond(HttpStatusCode.BadRequest, "No new name sent.")
        // make sure we're not moving to an empty string (root /tags dir)
        if (newName.isEmpty()) {
            return@patch call.respond(HttpStatusCode.BadRequest, "Can't have an empty tag!")
        }
        // TODO: allow this
        // can't move fruit/ to fruit/apple!
        if (newName.startsWith("${tag.fullName()}/")) {
            return@patch call.respond(HttpStatusCode.BadRequest, "Can't move a tag to one of its children!")
        }
        if (!tagController.renameTag(tag, newName))
            return@patch call.respond(HttpStatusCode.InternalServerError, "Invalid new name.")
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.deleteTagRoute() {
    delete("{name}") {
        val tag = ensureTagExists(call) ?: return@delete
        tagController.deleteTag(tag)
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.tagRouting() {
    route("/tag") {
        getTagRoute()
        createTagRoute()
        renameTagRoute()
        deleteTagRoute()
    }
}
