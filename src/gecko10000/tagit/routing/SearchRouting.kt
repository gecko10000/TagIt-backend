package gecko10000.tagit.routing

import gecko10000.tagit.objects.Nameable
import gecko10000.tagit.savedFiles
import gecko10000.tagit.tags
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.util.function.Predicate

fun Route.searchRouting() {
    route("/search") {
        get {
            search()
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.search() {
    val searchInput = call.request.queryParameters["q"] ?: return call.respond(HttpStatusCode.BadRequest, "Search input not provided.")
    val parsedSearch = parseSearchInput(searchInput)
    val foundTags = tags.filterValues { parsedSearch.test(it) }
    val foundFiles = savedFiles.filterValues { parsedSearch.test(it) }
    val response = mapOf<String, JsonElement>(
        "tags" to JsonArray(foundTags.map { Json.encodeToJsonElement(it) }),
        "files" to JsonArray(foundFiles.map{ Json.encodeToJsonElement(it) })
    )
    call.respond(response)
}

private fun parseSearchInput(input: String): Predicate<Nameable> {
    return Predicate {
        true
    }
}
