package gecko10000.tagit.routing

import gecko10000.tagit.fileController
import gecko10000.tagit.json.mapper.JsonMapper
import gecko10000.tagit.json.`object`.JsonSavedFile
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.model.enum.TagOrder
import gecko10000.tagit.model.enum.tagsReversed
import gecko10000.tagit.search.SearchQueryPredicateMapper
import gecko10000.tagit.tagController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import redempt.redlex.exception.LexException
import kotlin.math.min

private val searchQueryPredicateMapper = SearchQueryPredicateMapper()

private fun Route.simpleTagSearchRoute() {
    get("tags") {
        val searchInput = call.request.queryParameters["q"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            "Search input not provided."
        )
        val foundTags =
            tagController.readOnlyTagMap().values.filter { it.fullName().contains(searchInput) }
                .sortedBy { it.fullName().length }.toList()
        call.respondJson(foundTags)
    }
}

private val errorRegex = Regex("column ([0-9]+)")

private suspend fun handleLexException(call: ApplicationCall, input: String, ex: LexException) {
    val message = ex.message ?: return run {
        call.respond(HttpStatusCode.InternalServerError, "Parser error message was empty.")
    }
    val index = errorRegex.find(message)?.groups?.get(1)?.value ?: return run {
        call.respond(HttpStatusCode.InternalServerError, "Error regex matcher failed.")
    }
    // string is 1-indexed so we need to subtract 1 from the index that errors
    val normalizedIndex = index.toIntOrNull()?.let { min(it, input.length) }?.dec()
    call.respondJson(
        mapOf("index" to normalizedIndex.toString()),
        statusCode = HttpStatusCode.UnprocessableEntity
    )
}

private fun Route.searchFilesRoute() {
    get("files") {
        val searchInput = call.request.queryParameters["q"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            "Search input not provided."
        )
        if (searchInput.isEmpty()) return@get call.respondJson(listOf<JsonSavedFile>())
        val parsedSearch = try {
            searchQueryPredicateMapper.apply(searchInput)
        } catch (ex: LexException) {
            handleLexException(call, searchInput, ex)
            return@get
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        val foundFiles = fileController.readOnlyFileMap().values.filter { parsedSearch.test(it) }
        val headers = call.request.headers
        call.respondJson(foundFiles.map { JsonMapper.SAVED_FILE(it, TagOrder.get(headers), headers.tagsReversed()) })
    }
}

fun Route.searchRouting() {
    route("/search") {
        searchFilesRoute()
        simpleTagSearchRoute()
    }
}
