package gecko10000.tagit.serializers

import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ContentTypeSerializer : KSerializer<ContentType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ContentType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ContentType) = encoder.encodeString(value.withoutParameters().toString())
    override fun deserialize(decoder: Decoder) = ContentType.parse(decoder.decodeString())
}
