package gecko10000.tagit

import gecko10000.tagit.controller.*
import gecko10000.tagit.misc.Config
import gecko10000.tagit.misc.DataDirectory
import gecko10000.tagit.misc.SavedFileMap
import gecko10000.tagit.model.Tag
import kotlinx.coroutines.sync.Mutex
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFprobe
import java.util.*
import java.util.concurrent.ConcurrentHashMap

val config = Config()
val dataDirectory = DataDirectory()
val ffprobe = FFprobe()
val ffmpeg = FFmpeg()
val mutex = Mutex()

// we make these private so they can't be modified externally
// as that would risk a break in the structure
private val files = SavedFileMap()
private val tags = ConcurrentHashMap<UUID, Tag>()

// note: dimensions and thumbnail controllers should be instantiated first as
// they add listeners to the file map. loading files before adding the listeners
// will cause issues.
val dimensionsController = DimensionsController(files)
val thumbnailController = ThumbnailController(files)
val fileController = FileController(files, tags)
val tagController = TagController(files, tags)
val db = DatabaseController()
val server = ServerController()

fun main() {
    server.create().start(wait = true)
}
