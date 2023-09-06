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
        val outputFileName = "${savedFile.file.nameWithoutExtension}.$thumbnailFormat"
        return dataDirectory.thumbnail.resolve(outputFileName)
    }

    private fun makeImageThumb(savedFile: SavedFile): File {
        val outputFile = getOutputFile(savedFile)
        Thumbnails.of(savedFile.file)
            .size(thumbnailSize, thumbnailSize)
            .crop(Positions.CENTER)
            .toFile(outputFile)
        return outputFile
    }

    private fun makeVideoThumb(savedFile: SavedFile): File {
        val file = savedFile.file
        val outputFile = getOutputFile(savedFile)
        val builder = FFmpegBuilder()
            .addExtraArgs("-discard", "nokey")
            .addInput(file.path)
            .setVideoFilter("thumbnail,scale=$thumbnailSize:$thumbnailSize")
            .addOutput(outputFile.path)
            .setFrames(1)
            .done()
        ffmpeg.run(builder)
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
