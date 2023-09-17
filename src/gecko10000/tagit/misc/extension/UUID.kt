package gecko10000.tagit.misc.extension

import java.util.*

// Unfortunately, it's not possible to create a companion object extension for a
// class that doesn't have it. We'll have to settle for this for now.
fun uuidFromStringSafe(string: String?): UUID? {
    string ?: return null
    return try {
        UUID.fromString(string)
    } catch (ex: IllegalArgumentException) {
        null
    }
}
