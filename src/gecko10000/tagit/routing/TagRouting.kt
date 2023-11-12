package gecko10000.tagit.routing

import gecko10000.tagit.json.mapper.JsonMapper
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.misc.extension.uuidFromStringSafe
import gecko10000.tagit.model.Tag
import gecko10000.tagit.model.enum.FileOrder
import gecko10000.tagit.model.enum.TagOrder
import gecko10000.tagit.model.enum.filesReversed
import gecko10000.tagit.model.enum.tagsReversed
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
        val headers = call.request.headers
        val tagOrder = TagOrder.get(headers)
        val tagsReversed = headers.tagsReversed()
        val dummyTag = Tag(name = "", children = roots)
        // no files so order/reverse doesn't matter
        call.respondJson(JsonMapper.TAG(dummyTag, tagOrder, tagsReversed, FileOrder.MODIFICATION_DATE, false))
    }
    get("{uuid}") {
        val tag = ensureTagExists(call) ?: return@get
        val headers = call.request.headers
        val tagOrder = TagOrder.get(headers)
        val tagsReversed = headers.tagsReversed()
        val fileOrder = FileOrder.get(headers)
        val filesReversed = headers.filesReversed()
        call.respondJson(JsonMapper.TAG(tag, tagOrder, tagsReversed, fileOrder, filesReversed))
    }
}

private fun Route.createTagRoute() {
    post("{name}") {
        val name = getTagName(call)!!
        mutex.withLock {
            val existing = tagController.readOnlyTagMap().values.firstOrNull { it.fullName() == name }
            existing?.run { return@post call.respond(HttpStatusCode.BadRequest, "Tag already exists.") }
            tagController.createTag(name)
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
            val newTag = tagController.readOnlyTagMap().values.firstOrNull { it.fullName() == newName }
            newTag ?: return@patch call.respond(HttpStatusCode.InternalServerError, "Tag renamed but not found.")
            val headers = call.request.headers
            val tagOrder = TagOrder.get(headers)
            val tagsReversed = headers.tagsReversed()
            val fileOrder = FileOrder.get(headers)
            val filesReversed = headers.filesReversed()
            call.respondJson(JsonMapper.TAG(newTag, tagOrder, tagsReversed, fileOrder, filesReversed))
        }
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
