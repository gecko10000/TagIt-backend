package gecko10000.tagit.serializers

import gecko10000.tagit.misc.fileDirectory
import io.ktor.server.http.content.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.File

class FileSerializer : KSerializer<File> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("File") {
        element<String>("name")
        element<String>("mimeType")
        element<Long>("modificationDate")
        element<Long>("fileSize")
    }

    override fun serialize(encoder: Encoder, value: File) {
        val structure = encoder.beginStructure(descriptor)
        structure.encodeStringElement(descriptor, 0, value.name)
        structure.encodeStringElement(descriptor, 1, LocalFileContent(value).contentType.withoutParameters().toString())
        structure.encodeLongElement(descriptor, 2, value.lastModified())
        structure.encodeLongElement(descriptor, 3, value.length())
        structure.endStructure(descriptor)
    }
    override fun deserialize(decoder: Decoder) = File(fileDirectory + decoder.decodeString())
}
