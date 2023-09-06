package gecko10000.tagit

import gecko10000.tagit.controller.*
import gecko10000.tagit.misc.Config
import gecko10000.tagit.misc.DataDirectory
import gecko10000.tagit.misc.SavedFileMap
import gecko10000.tagit.model.Tag
import java.util.concurrent.ConcurrentHashMap

val config = Config()
val dataDirectory = DataDirectory()

// we make these private so they can't be modified externally
// as that would risk a break in the structure
private val files = SavedFileMap()
private val tags = ConcurrentHashMap<String, Tag>()

// note: dimension order should be instantiated first as it adds
// listeners to the file map. loading files before adding the listeners
// will cause issues.
val dimensionsController = DimensionsController(files)
val fileController = FileController(files, tags)
val tagController = TagController(files, tags)
val db = DatabaseController()
val server = ServerController()

fun main() {
    server.create().start(wait = true)
}
