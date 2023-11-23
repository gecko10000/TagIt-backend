package gecko10000.tagit.model.enum

import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import io.ktor.http.*
import java.util.*

enum class TagOrder(val comparator: Comparator<Tag>) {
    TAG_NAME(compareBy { it.name }),
    NUM_SUBTAGS(compareBy { it.getAllChildren().size }),
    NUM_FILES(compareBy { it.getAllFiles().size }),
    ;

    companion object {
        fun get(headers: Headers): TagOrder {
            return try {
                headers["tagOrder"]?.uppercase(Locale.getDefault())?.let { TagOrder.valueOf(it) } ?: TAG_NAME
            } catch (ex: IllegalArgumentException) {
                TAG_NAME
            }
        }

    }
}

private val modificationDateComparator = compareBy<SavedFile> { it.file.lastModified() }

enum class FileOrder(val comparator: Comparator<SavedFile>) {
    FILE_NAME(compareBy { it.file.name }),
    MODIFICATION_DATE(modificationDateComparator),
    FILE_SIZE(compareBy<SavedFile> { it.file.length() }.thenComparing(modificationDateComparator)),
    NUM_TAGS(compareBy<SavedFile> { it.tags.size }.thenComparing(modificationDateComparator)),
    FILE_TYPE(compareBy<SavedFile> { it.file.extension }.thenComparing(modificationDateComparator)),
    ;

    companion object {
        fun get(headers: Headers): FileOrder {
            return try {
                headers["fileOrder"]?.uppercase(Locale.getDefault())?.let { FileOrder.valueOf(it) }
                    ?: MODIFICATION_DATE
            } catch (ex: IllegalArgumentException) {
                MODIFICATION_DATE
            }
        }
    }
}

fun Headers.tagsReversed(): Boolean {
    return this["tagsReversed"] == "true" // default false if not provided or something other than "true"
}

fun Headers.filesReversed(): Boolean {
    return this["filesReversed"] == "true" // default false if not provided or something other than "true"
}
