package gecko10000.tagit.json.`object`

import kotlinx.serialization.Serializable

@Serializable
data class JsonTagCounts(
    val tags: Int,
    val totalTags: Int,
    val files: Int,
    val totalFiles: Int,
)
