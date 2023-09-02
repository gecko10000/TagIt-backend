package gecko10000.tagit.json.`object`

import kotlinx.serialization.Serializable

@Serializable
data class JsonTag(
    val name: String,
    val parent: String?,
    val children: Set<String>,
    val files: Set<String>,
    val totalFileCount: Int,
)
