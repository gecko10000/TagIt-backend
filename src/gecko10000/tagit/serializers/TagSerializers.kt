package gecko10000.tagit.serializers

import gecko10000.tagit.objects.Tag
import gecko10000.tagit.tags
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class TagStringSerializer : KSerializer<Tag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Tag", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Tag) = encoder.encodeString(value.fullName())
    override fun deserialize(decoder: Decoder) = tags[decoder.decodeString()] ?: throw SerializationException("Tag not found: ${decoder.decodeString()}")
}

class TagNameSerializer : KSerializer<Tag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Tag", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Tag) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder) = throw NotImplementedError()
}
