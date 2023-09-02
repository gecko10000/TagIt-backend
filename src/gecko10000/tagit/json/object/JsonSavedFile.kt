package gecko10000.tagit.json.`object`

import gecko10000.tagit.json.enum.MediaType
import kotlinx.serialization.Serializable

@Serializable
data class JsonSavedFile(
    val name: String,
    val mediaType: MediaType,
    val modificationDate: Long,
    val fileSize: Long,
    val thumbnail: String?,
    val size: JsonSize?,
    val tags: Set<String>,
)
