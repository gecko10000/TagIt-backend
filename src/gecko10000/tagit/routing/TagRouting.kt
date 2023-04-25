package gecko10000.tagit.routing

import gecko10000.tagit.fileManager
import gecko10000.tagit.misc.respondJson
import gecko10000.tagit.objects.Tag
import gecko10000.tagit.tags
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

private fun getTagName(call: ApplicationCall) = call.parameters["name"]?.trimEnd('/')

private suspend fun ensureTagExists(call: ApplicationCall): Tag? {
    val name = getTagName(call)
    val existing = tags[name]
    // TODO: check filesystem?
    existing ?: call.respond(HttpStatusCode.NotFound, "Tag not found.")
    return existing
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getRoot() {
    // need sortedBy since we're looking at `tags`
    val roots = tags.filter { it.value.parent == null }.values.sortedBy { it.name }
    call.respondJson(mapOf("children" to roots))
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getTag() {
    val tag = ensureTagExists(call) ?: return
    call.respondJson(tag)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.listTag() {
    val tag = ensureTagExists(call) ?: return
    val response = mapOf<String, JsonElement>(
        "children" to JsonArray(tag.children.map { Json.encodeToJsonElement(it) }),
        "files" to JsonArray(tag.files.map{ Json.encodeToJsonElement(it) })
    )
    call.respondJson(response)

}

private suspend fun PipelineContext<Unit, ApplicationCall>.createTag() {
    val name = getTagName(call)!!
    tags[name]?.run { return call.respond(HttpStatusCode.BadRequest, "Tag already exists.") }
    fileManager.createTag(name) ?: return call.respond(HttpStatusCode.InternalServerError, "Could not create tag.")
    println(name)
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.renameTag() {
    val tag = ensureTagExists(call) ?: return
    val params = call.receiveParameters()
    val newName = params["name"]?.trimEnd('/') ?: return call.respond(HttpStatusCode.BadRequest, "No new name sent.")
    // make sure we're not moving to an empty string (root /tags dir)
    if (newName.isEmpty()) {
        return call.respond(HttpStatusCode.BadRequest, "Can't have an empty tag!")
    }
    // TODO: allow this
    // can't move fruit/ to fruit/apple!
    if (newName.startsWith("${tag.fullName()}/")) {
        return call.respond(HttpStatusCode.BadRequest, "Can't move a tag to one of its children!")
    }
    if (!fileManager.renameTag(tag, newName))
        return call.respond(HttpStatusCode.InternalServerError, "Invalid new name.")
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.deleteTag() {
    val tag = ensureTagExists(call) ?: return
    fileManager.deleteTag(tag)
    call.respond(HttpStatusCode.OK)
}

fun Route.tagRouting() {
    route("/tag") {
        get {
            this.getRoot()
        }
        get("{name}") {
            this.getTag()
        }
        get("{name}/list") {
            this.listTag()
        }
        post("{name}") {
            this.createTag()
        }
        patch("{name}") {
            this.renameTag()
        }
        delete("{name}") {
            this.deleteTag()
        }
    }
}
