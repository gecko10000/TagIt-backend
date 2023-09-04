package gecko10000.tagit.json.`object`

import gecko10000.tagit.model.enum.MediaType
import kotlinx.serialization.Serializable

@Serializable
data class JsonSavedFile(
    val name: String,
    val mediaType: MediaType,
    val modificationDate: Long,
    val fileSize: Long,
    val thumbnail: String?,
    val dimensions: JsonDimensions?,
    val tags: Set<String>,
)
