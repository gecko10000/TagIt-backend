package gecko10000.tagit.json.`object`

import gecko10000.tagit.json.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class JsonTag(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val name: String,
    @Serializable(with = UUIDSerializer::class)
    val parentUUID: UUID?,
    val parentName: String?,
    val children: Collection<JsonChildTag>,
    val files: Collection<JsonSavedFile>,
    val totalFileCount: Int,
)
