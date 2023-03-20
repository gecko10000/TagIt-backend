package gecko10000.tagit.serializers

import gecko10000.tagit.objects.Tag
import gecko10000.tagit.tags
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class NullableTagStringSerializer : KSerializer<Tag?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NullableTag", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Tag?) = value?.run { encoder.encodeString(value.fullName()) } ?: encoder.encodeNull()
    override fun deserialize(decoder: Decoder) = if (decoder.decodeNotNullMark()) tags[decoder.decodeString()] else decoder.decodeNull()
}
