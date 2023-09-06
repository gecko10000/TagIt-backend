package gecko10000.tagit.controller

import gecko10000.tagit.model.Dimensions
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.enum.MediaType
import net.bramp.ffmpeg.FFprobe
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream

class DimensionsController {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val dimensionsMap = ConcurrentHashMap<String, Dimensions>()

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
        val stream = FFprobe().probe(savedFile.file.path).streams.ifEmpty { return null }[0]
        return Dimensions(stream.width, stream.height)
    }

    fun determineSize(savedFile: SavedFile) {
        val name = savedFile.file.name
        if (dimensionsMap.containsKey(name)) return
        val dimensions = when (savedFile.mediaType) {
            MediaType.IMAGE -> getImageDimensions(savedFile)
            MediaType.VIDEO -> getVideoDimensions(savedFile)
            else -> null
        }
        dimensions?.let {
            log.info("Determined dimensions for {}: {}x{}", name, dimensions.width, dimensions.height)
            dimensionsMap[name] = dimensions
        }
        //}
    }

    fun getDimensions(savedFile: SavedFile) = dimensionsMap[savedFile.file.name]

    fun removeSavedFile(savedFile: SavedFile) {
        dimensionsMap.remove(savedFile.file.name)
    }
}
