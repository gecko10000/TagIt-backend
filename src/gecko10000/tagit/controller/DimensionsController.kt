package gecko10000.tagit.controller

import gecko10000.tagit.ffprobe
import gecko10000.tagit.misc.SavedFileMap
import gecko10000.tagit.model.Dimensions
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.enum.MediaType
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream

class DimensionsController(files: SavedFileMap) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val dimensionsMap = ConcurrentHashMap<UUID, Dimensions>()

    init {
        files.addPutListener { determineSize(it) }
        files.addRemoveListener { removeSavedFile(it) }
    }

    private fun getImageDimensions(savedFile: SavedFile): Dimensions? {
        val readers = ImageIO.getImageReadersByMIMEType(savedFile.mimeType!!)
        readers.forEach { reader ->
            val stream = FileImageInputStream(savedFile.file)
            try {
                reader.input = stream
                val width = reader.getWidth(reader.minIndex)
                val height = reader.getHeight(reader.minIndex)
                return Dimensions(width, height)
            } catch (_: IOException) {
            } finally {
                stream.close()
                reader.dispose()
            }
        }
        return null
    }

    private fun getVideoDimensions(savedFile: SavedFile): Dimensions? {
        val stream = ffprobe.probe(savedFile.file.path).streams.ifEmpty { return null }[0]
        return Dimensions(stream.width, stream.height)
    }

    private fun determineSize(savedFile: SavedFile) {
        val uuid = savedFile.uuid
        if (dimensionsMap.containsKey(uuid)) return
        val dimensions = when (savedFile.mediaType) {
            MediaType.IMAGE -> getImageDimensions(savedFile)
            MediaType.VIDEO -> getVideoDimensions(savedFile)
            else -> null
        }
        dimensions?.let {
            // loaded every startup, no need to log repeatedly
            // log.info("Determined dimensions for {}: {}x{}", name, dimensions.width, dimensions.height)
            dimensionsMap[uuid] = dimensions
        }
    }

    fun getDimensions(savedFile: SavedFile) = dimensionsMap[savedFile.uuid]

    private fun removeSavedFile(savedFile: SavedFile) {
        dimensionsMap.remove(savedFile.uuid)
    }
}
