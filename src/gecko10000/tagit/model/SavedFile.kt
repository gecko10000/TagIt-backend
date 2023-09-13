package gecko10000.tagit.model

import gecko10000.tagit.model.enum.MediaType
import gecko10000.tagit.model.mapper.ModelMapper
import java.io.File
import java.nio.file.Files
import java.util.*

data class SavedFile(val file: File, val tags: Set<String> = setOf()) {
    val uuid = UUID.randomUUID()
    val mimeType: String? = Files.probeContentType(file.toPath())
    val mediaType = run {
        mimeType?.let { ModelMapper.MEDIA_TYPE.apply(it) } ?: MediaType.UNKNOWN
    }
}
/*
@Serializable
data class OldSavedFile(
    val name: String,
    @Transient
    val file: File = File("$fileDirectory$name"),
    @Serializable(with = ContentTypeSerializer::class)
    val mimeType: ContentType,
    val modificationDate: Long,
    val fileSize: Long,
    val thumbnail: Boolean,
    val size: gecko10000.tagit.json.objects.Size?,
    val tags: Set<@Serializable(with = TagStringSerializer::class) TagEntity> = setOf()
) {
    companion object {
        fun hasThumbnail(file: File): Boolean {
            val contentType = LocalFileContent(file).contentType
            return contentType.match(ContentType.Image.Any) || contentType.match(ContentType.Video.Any)
        }
    }
    constructor(file: File, tags: Set<TagEntity> = setOf()) : this(file.name, file, LocalFileContent(file).contentType, file.lastModified(), file.length(), hasThumbnail(file),
        Size.get(file), tags)

    private fun makeImageThumbnail() {

    }

    private fun makeVideoThumbnail() {
        val output = File("$thumbnailDirectory${file.name}.png")
        if (output.exists()) return
        println("Making video thumbnail for ${file.name}")
        val builder = FFmpegBuilder().addInput(file.path)
            .addOutput(output.path)
            .also { it.video_frames = 1 }
            .done()
        FFmpegExecutor().createJob(builder).run()
    }

    private fun makeThumbnail() {
        if (mimeType.match(ContentType.Image.Any)) {
            makeImageThumbnail()
        } else if (mimeType.match(ContentType.Video.Any)) {
            makeVideoThumbnail()
        } else {
            throw IllegalArgumentException("File type $mimeType cannot have a thumbnail, yet makeThumbnail was called.")
        }
    }

    init {
        if (thumbnail) {
            makeThumbnail()
        }
    }
}

@Serializable
data class Size(val width: Int, val height: Int) {
    companion object {

        private fun getImageSize(file: File): gecko10000.tagit.json.objects.Size? {
            val mimeType = LocalFileContent(file).contentType.withoutParameters().toString()
            val readers = ImageIO.getImageReadersByMIMEType(mimeType)
            readers.forEach { reader ->
                val stream = FileImageInputStream(file)
                try {
                    reader.input = stream
                    val width = reader.getWidth(reader.minIndex)
                    val height = reader.getHeight(reader.minIndex)
                    return gecko10000.tagit.json.objects.Size(width, height)
                } catch (_: IOException) {} finally {
                    stream.close()
                    reader.dispose()
                }
            }
            return null
        }

        private fun getVideoSize(file: File): gecko10000.tagit.json.objects.Size? {

        }

        fun get(file: File): gecko10000.tagit.json.objects.Size? {
            val contentType = LocalFileContent(file).contentType
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
*/
