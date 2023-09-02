package gecko10000.tagit.json.`object`

import kotlinx.serialization.Serializable

@Serializable
data class JsonTag(
    val name: String,
    val parent: String?,
    val children: Set<JsonChildTag>,
    val files: Set<JsonSavedFile>,
    val totalFileCount: Int,
)
