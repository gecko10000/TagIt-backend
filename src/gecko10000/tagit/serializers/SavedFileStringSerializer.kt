package gecko10000.tagit.serializers

import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.savedFiles
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class SavedFileStringSerializer : KSerializer<SavedFile> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SavedFile", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SavedFile) = encoder.encodeString(value.file.name)
    override fun deserialize(decoder: Decoder) =
        savedFiles[decoder.decodeString()] ?: throw SerializationException("File not found: ${decoder.decodeString()}")
}
