package gecko10000.tagit.objects

import gecko10000.tagit.misc.fileDirectory
import gecko10000.tagit.serializers.ContentTypeSerializer
import gecko10000.tagit.serializers.TagStringSerializer
import io.ktor.http.*
import io.ktor.server.http.content.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.bramp.ffmpeg.FFprobe
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream

@Serializable
data class SavedFile(
    val name: String,
    @Transient
    val file: File = File("$fileDirectory$name"),
    @Serializable(with = ContentTypeSerializer::class)
    val mimeType: ContentType,
    val modificationDate: Long,
    val fileSize: Long,
    val thumbnail: Boolean,
    val size: Size?,
    val tags: Set<@Serializable(with = TagStringSerializer::class) Tag> = setOf()
) {
    companion object {
        fun hasThumbnail(file: File): Boolean {
            val contentType = LocalFileContent(file).contentType
            return contentType.match(ContentType.Image.Any) || contentType.match(ContentType.Video.Any)
        }
    }
    constructor(file: File, tags: Set<Tag> = setOf()) : this(file.name, file, LocalFileContent(file).contentType, file.lastModified(), file.length(), hasThumbnail(file), Size.get(file), tags)
}

@Serializable
data class Size(val width: Int, val height: Int) {
    companion object {

        private fun getImageSize(file: File): Size? {
            val mimeType = LocalFileContent(file).contentType.withoutParameters().toString()
            val readers = ImageIO.getImageReadersByMIMEType(mimeType)
            println("$readers readers found")
            readers.forEach { reader ->
                val stream = FileImageInputStream(file)
                try {
                    reader.input = stream
                    val width = reader.getWidth(reader.minIndex)
                    val height = reader.getHeight(reader.minIndex)
                    return Size(width, height)
                } catch (_: IOException) {} finally {
                    stream.close()
                    reader.dispose()
                }
            }
            return null
        }

        private fun getVideoSize(file: File): Size? {
            val stream = FFprobe().probe("$fileDirectory${file.name}").streams.ifEmpty { return null }[0]
            return Size(stream.width, stream.height)
        }

        fun get(file: File): Size? {
            val contentType = LocalFileContent(file).contentType
            println("$contentType, ${contentType.match(ContentType.Image.Any)}")
            if (contentType.match(ContentType.Image.Any)) {
                return getImageSize(file)
            }
            if (contentType.match(ContentType.Video.Any)) {
                return getVideoSize(file)
            }
            return null
        }
    }
}
