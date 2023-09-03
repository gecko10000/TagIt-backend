package gecko10000.tagit.json.`object`

import kotlinx.serialization.Serializable

/*
This class is used in JsonTag so info can
be displayed in the file browser properly.
 */
@Serializable
data class JsonChildTag(
    val name: String,
    val parent: String?,
    val counts: JsonTagCounts,
)
