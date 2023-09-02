package gecko10000.tagit.search

import gecko10000.tagit.model.SavedFile
import java.util.function.Function
import java.util.function.Predicate

class SearchQueryPredicateMapper : Function<String, Predicate<SavedFile>?> {
    override fun apply(query: String): Predicate<SavedFile> {
        @Suppress("UNCHECKED_CAST")
        return parser.parse(query) as Predicate<SavedFile>
    }
}
