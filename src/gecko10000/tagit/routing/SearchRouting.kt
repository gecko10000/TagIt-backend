package gecko10000.tagit.routing

import gecko10000.tagit.misc.respondJson
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
import redempt.redlex.bnf.BNFParser
import redempt.redlex.parser.Parser
import redempt.redlex.parser.ParserComponent
import redempt.redlex.processing.CullStrategy
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
        "tags" to JsonArray(foundTags.map { Json.encodeToJsonElement(it.value) }),
        "files" to JsonArray(foundFiles.map{ Json.encodeToJsonElement(it.value) })
    )
    call.respondJson(response)
}

private val parser = Parser.create(
    run {
        val lexer = BNFParser.createLexer(ClassLoader.getSystemClassLoader().getResourceAsStream("search.bnf"))
        lexer.setUnnamedRule(CullStrategy.LIFT_CHILDREN)
        lexer.setRuleByName(CullStrategy.DELETE_ALL, "space")
        lexer
    },
    ParserComponent.mapChildren("query") {
        if (it.size != 3) it[0]
        else if (it[1] is BinaryOperator) {
            val q1 = it[0] as Predicate<Nameable>
            val q2 = it[2] as Predicate<Nameable>
            if (it[1] == BinaryOperator.AND) q1.and(q2) else q1.or(q2)
        }
        else it[1]
    },
    ParserComponent.mapToken("and") {
        BinaryOperator.AND
    },
    ParserComponent.mapToken("or") {
        BinaryOperator.OR
    },
    ParserComponent.mapToken("word") { token ->
        Predicate<Nameable> { token.value == it.name() }
    }
)

private fun parseSearchInput(input: String): Predicate<Nameable> {
    try {
        return parser.parse(input) as Predicate<Nameable>
    } catch (ex: Exception) {
        ex.printStackTrace()
        throw ex
    }
}

enum class BinaryOperator {
    AND,
    OR
}
