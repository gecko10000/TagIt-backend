package gecko10000.tagit.routing

import gecko10000.tagit.fileController
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.search.SearchQueryPredicateMapper
import gecko10000.tagit.tagController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import redempt.redlex.exception.LexException

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

private suspend fun handleLexException(call: ApplicationCall, ex: LexException) {
    val message = ex.message ?: return run {
        call.respond(HttpStatusCode.InternalServerError, "Parser error message was empty.")
    }
    val index = errorRegex.find(message)?.groups?.get(1)?.value ?: return run {
        call.respond(HttpStatusCode.InternalServerError, "Error regex matcher failed.")
    }
    // string is 1-indexed so we need to subtract 1 from the index that errors
    call.respondJson(
        mapOf("index" to index.toIntOrNull()?.dec().toString()),
        statusCode = HttpStatusCode.UnprocessableEntity
    )
}

private fun Route.searchFilesRoute() {
    get("files") {
        val searchInput = call.request.queryParameters["q"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            "Search input not provided."
        )
        if (searchInput.isEmpty()) return@get call.respondJson(listOf<SavedFile>())
        val parsedSearch = try {
            searchQueryPredicateMapper.apply(searchInput)
        } catch (ex: LexException) {
            handleLexException(call, ex)
            return@get
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        val foundFiles = fileController.readOnlyFileMap().values.filter { parsedSearch.test(it) }.toList()
        call.respondJson(foundFiles)
    }
}

fun Route.searchRouting() {
    route("/search") {
        searchFilesRoute()
        simpleTagSearchRoute()
    }
}


