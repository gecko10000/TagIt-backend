package gecko10000.tagit.search

import gecko10000.tagit.model.SavedFile
import redempt.redlex.bnf.BNFParser
import redempt.redlex.parser.Parser
import redempt.redlex.parser.ParserComponent
import redempt.redlex.processing.CullStrategy
import java.util.function.Predicate

// simply returns true if the operator is an "and" and false otherwise.
private fun isAnd(str: String): Boolean = str.equals("and", ignoreCase = true) || str == "&" || str == "&&"

@Suppress("UNCHECKED_CAST") // if i don't see it, it's not there
val parser = Parser.create(
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
        Predicate<SavedFile> { it.file.name.contains(sub) }
    },
    ParserComponent.mapString("tag") { s ->
        Predicate<SavedFile> { it.tags.any { t -> t.fullName().contains(s) } }
    },
    ParserComponent.mapChildren("not") {
        Predicate.not(it[0] as Predicate<SavedFile>)
    },
    ParserComponent.mapString("word") { it },
)
