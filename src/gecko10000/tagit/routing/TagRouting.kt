package gecko10000.tagit.routing

import gecko10000.tagit.json.mapper.JsonMapper
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.misc.extension.uuidFromStringSafe
import gecko10000.tagit.model.Tag
import gecko10000.tagit.mutex
import gecko10000.tagit.tagController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.sync.withLock

private fun getTagName(call: ApplicationCall) = call.parameters["name"]?.trimEnd('/')

private suspend fun ensureTagExists(call: ApplicationCall): Tag? {
    val uuidString = call.parameters["uuid"] ?: run {
        call.respond(HttpStatusCode.BadRequest, "No tag UUID sent.")
        return null
    }
    val uuid = uuidFromStringSafe(uuidString) ?: run {
        call.respond(HttpStatusCode.BadRequest, "Invalid UUID sent.")
        return null
    }
    val existing = tagController[uuid]
    existing ?: call.respond(HttpStatusCode.NotFound, "Tag not found.")
    return existing
}

private fun Route.getTagRoute() {
    get {
        val roots =
            tagController.readOnlyTagMap()
                .filter { it.value.parent == null }
                .values
                .map { it.uuid }
                .toSet()
        val dummyTag = Tag(name = "", children = roots)
        call.respondJson(JsonMapper.TAG.apply(dummyTag))
    }
    get("{uuid}") {
        val tag = ensureTagExists(call) ?: return@get
        call.respondJson(JsonMapper.TAG.apply(tag))
    }
}

private fun Route.createTagRoute() {
    post("{name}") {
        val name = getTagName(call)!!
        mutex.withLock {
            val existing = tagController.readOnlyTagMap().values.firstOrNull { it.fullName() == name }
            existing?.run { return@post call.respond(HttpStatusCode.BadRequest, "Tag already exists.") }
            tagController.createTag(name) ?: return@post call.respond(
                HttpStatusCode.InternalServerError,
                "Could not create tag."
            )
        }
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.renameTagRoute() {
    patch("{uuid}") {
        mutex.withLock {
            val tag = ensureTagExists(call) ?: return@patch
            val params = call.receiveParameters()
            val newName =
                params["name"]?.trimEnd('/') ?: return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    "No new name sent."
                )
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
                return@patch call.respond(HttpStatusCode.InternalServerError, "Could not rename tag.")
        }
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.deleteTagRoute() {
    delete("{uuid}") {
        mutex.withLock {
            val tag = ensureTagExists(call) ?: return@delete
            tagController.deleteTag(tag)
        }
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
