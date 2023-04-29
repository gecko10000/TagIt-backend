package gecko10000.tagit.routing

import gecko10000.tagit.misc.respondJson
import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.savedFiles
import gecko10000.tagit.tags
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import redempt.redlex.bnf.BNFParser
import redempt.redlex.exception.LexException
import redempt.redlex.parser.Parser
import redempt.redlex.parser.ParserComponent
import redempt.redlex.processing.CullStrategy
import java.util.function.Predicate



fun Route.searchRouting() {
    route("/search") {
        get("files") {
            searchFiles()
        }
        get("tags") {
            searchTags()
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.searchTags() {
    val searchInput = call.request.queryParameters["q"] ?: return call.respond(HttpStatusCode.BadRequest, "Search input not provided.")
    val foundTags = tags.values.filter { it.fullName().contains(searchInput) }.toList()
    call.respondJson(foundTags)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.searchFiles() {
    val searchInput = call.request.queryParameters["q"] ?: return call.respond(HttpStatusCode.BadRequest, "Search input not provided.")
    val parsedSearch = parseSearchInput(call, searchInput) ?: return
    val foundFiles = savedFiles.values.filter { parsedSearch.test(it) }.toList()
    call.respondJson(foundFiles)
}

// simply returns true if the operator is an "and" and false otherwise.
private fun isAnd(str: String): Boolean = str.equals("and", ignoreCase = true) || str == "&" || str == "&&"

@Suppress("UNCHECKED_CAST") // if i don't see it, it's not there
private val parser = Parser.create(
    run {
        val lexer = BNFParser.createLexer(ClassLoader.getSystemClassLoader().getResourceAsStream("search.bnf"))
        lexer.setUnnamedRule(CullStrategy.LIFT_CHILDREN)
        lexer.setRetainStringLiterals(false)
        lexer.setRuleByName(CullStrategy.DELETE_ALL, "sep")
        lexer
    },
    ParserComponent.mapChildren("query") {
        //if (it.size != 3) it[0]
        var predicate = it[0] as Predicate<SavedFile>
        lateinit var operator: BinaryOperator
        for (i in IntRange(1, it.size - 1)) {
            // query
            if (i % 2 == 0) {
                predicate = operator.apply(predicate, it[i] as Predicate<SavedFile>)
            } else operator = it[i] as BinaryOperator
        }
        predicate
    },
    ParserComponent.mapChildren("term") { it[0] },
    ParserComponent.mapString("operator") {
        if (isAnd(it.trim())) BinaryOperator.AND else BinaryOperator.OR
    },
    ParserComponent.mapString("file") { s ->
        // we get file:[ ]*<filename> so we have to remove the leading text
        val sub = s.substringAfter(':').trimStart()
        Predicate<SavedFile>{ it.name().contains(sub) }
    },
    ParserComponent.mapString("tag") { s ->
        Predicate<SavedFile>{ it.tags.any { t -> t.fullName().contains(s) }}
    },
    ParserComponent.mapChildren("not") {
        Predicate.not(it[0] as Predicate<SavedFile>)
    },
    ParserComponent.mapString("word") { it },
)

private val errorRegex = Regex("column ([0-9]+)")
private suspend fun parseSearchInput(call: ApplicationCall, input: String): Predicate<SavedFile>? {
    try {
        @Suppress("UNCHECKED_CAST")
        return parser.parse(input) as Predicate<SavedFile>
    } catch (ex: LexException) {
        val message = ex.message ?: return run {
            call.respond(HttpStatusCode.InternalServerError, "Parser error message was empty.")
            null
        }
        val index = errorRegex.find(message)?.groups?.get(1)?.value ?: return run {
            call.respond(HttpStatusCode.InternalServerError, "Error regex matcher failed.")
            null
        }
        // string is 1-indexed so we need to subtract 1 from the index that errors
        call.respond(HttpStatusCode.UnprocessableEntity, index.toIntOrNull()?.dec().toString())
    } catch (ex: Exception) {
        ex.printStackTrace()
        throw ex
    }
    return null
}

enum class BinaryOperator {
    AND,
    OR,
    ;
    fun <T> apply(p1: Predicate<T>, p2: Predicate<T>): Predicate<T> = if (this == AND) p1.and(p2) else p1.or(p2)
}
