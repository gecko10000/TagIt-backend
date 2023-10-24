package gecko10000.tagit.json.`object`

import gecko10000.tagit.json.serializer.UUIDSerializer
import gecko10000.tagit.model.enum.MediaType
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class JsonSavedFile(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val name: String,
    val mediaType: MediaType,
    val modificationDate: Long,
    val fileSize: Long,
    val thumbnail: Boolean,
    val dimensions: JsonDimensions?,
    val tags: Collection<JsonChildTag>,
)
