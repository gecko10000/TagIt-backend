package gecko10000.tagit.json.objects

import gecko10000.tagit.json.enums.MediaType
import kotlinx.serialization.Serializable

@Serializable
data class JsonSavedFile(
    val name: String,
    val mimeType: MediaType,
    val modificationDate: Long,
    val fileSize: Long,
    val thumbnail: String?,
    val size: JsonSize?,
    val tags: Set<String>,
)
