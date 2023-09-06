package gecko10000.tagit.controller

import gecko10000.tagit.dataDirectory
import gecko10000.tagit.ffmpeg
import gecko10000.tagit.misc.SavedFileMap
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.enum.MediaType
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

const val thumbnailSize = 200
const val thumbnailFormat = "png"

class ThumbnailController(files: SavedFileMap) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val thumbnailMap = ConcurrentHashMap<String, File>()

    private fun checkForExistingThumb(savedFile: SavedFile): File? {
        val thumb = getOutputFile(savedFile)
        return if (thumb.exists()) thumb else null
    }

    private fun getOutputFile(savedFile: SavedFile): File {
        // note: we must use the full filename.
        // Without the extension, we may get collisions
        // like files screenshot.png and screenshot.jpg
        // having the same thumbnail filename.
        val outputFileName = "${savedFile.file.name}.$thumbnailFormat"
        return dataDirectory.thumbnail.resolve(outputFileName)
    }

    private fun convertThumb(input: File, output: File) {
        Thumbnails.of(input)
            .size(thumbnailSize, thumbnailSize)
            .crop(Positions.CENTER)
            .toFile(output)
    }

    private fun makeImageThumb(savedFile: SavedFile): File {
        val outputFile = getOutputFile(savedFile)
        convertThumb(savedFile.file, outputFile)
        return outputFile
    }

    private fun makeVideoThumb(savedFile: SavedFile): File {
        val file = savedFile.file
        // note: we use .<extension> for the suffix so ffmpeg recognizes the filetype
        val tempFile = File.createTempFile("tagit", ".$thumbnailFormat");
        val builder = FFmpegBuilder()
            .addExtraArgs("-discard", "nokey")
            .addInput(file.path)
            .setVideoFilter("thumbnail")
            .addOutput(tempFile.path)
            .setFrames(1)
            .done()
        ffmpeg.run(builder)

        val outputFile = getOutputFile(savedFile)
        convertThumb(tempFile, outputFile)
        tempFile.delete()
        return outputFile
    }

    private fun makeThumbnail(savedFile: SavedFile) {
        val name = savedFile.file.name
        if (thumbnailMap.containsKey(name)) return
        val existing = checkForExistingThumb(savedFile)
        existing?.let {
            thumbnailMap[name] = existing
            return
        }
        val thumbnail = when (savedFile.mediaType) {
            MediaType.IMAGE -> makeImageThumb(savedFile)
            MediaType.VIDEO -> makeVideoThumb(savedFile)
            else -> null
        }
        thumbnail?.let {
            log.info("Generated new thumbnail for {}.", name)
            thumbnailMap[name] = thumbnail
        }
    }

    fun getThumbnail(savedFile: SavedFile) = thumbnailMap[savedFile.file.name]

    private fun removeSavedFile(savedFile: SavedFile) {
        val thumb = thumbnailMap.remove(savedFile.file.name)
        thumb?.delete()
    }

    init {
        files.addPutListener { makeThumbnail(it) }
        files.addRemoveListener { removeSavedFile(it) }
    }
}
