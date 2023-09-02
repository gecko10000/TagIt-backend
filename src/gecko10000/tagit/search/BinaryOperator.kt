package gecko10000.tagit.search

import java.util.function.Predicate

enum class BinaryOperator {
    AND,
    OR,
    ;

    fun <T> apply(p1: Predicate<T>, p2: Predicate<T>): Predicate<T> = if (this == AND) p1.and(p2) else p1.or(p2)
}
