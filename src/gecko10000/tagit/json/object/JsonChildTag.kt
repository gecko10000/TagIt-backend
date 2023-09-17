package gecko10000.tagit.json.`object`

import gecko10000.tagit.json.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

/*
This class is used in JsonTag so info can
be displayed in the file browser properly.
 */
@Serializable
data class JsonChildTag(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val name: String,
    val parent: String?,
    val counts: JsonTagCounts,
)
