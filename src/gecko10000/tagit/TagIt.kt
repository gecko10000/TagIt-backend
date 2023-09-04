package gecko10000.tagit

import gecko10000.tagit.controller.*
import gecko10000.tagit.misc.Config
import gecko10000.tagit.misc.DataDirectory
import gecko10000.tagit.misc.SavedFileMap
import gecko10000.tagit.model.Tag
import java.util.concurrent.ConcurrentHashMap

val config = Config()
val dataDirectory = DataDirectory()
val dimensionsController = DimensionsController()

// we make these private so they can't be modified externally
// as that would risk a break in the structure
private val files = SavedFileMap(dimensionsController)
private val tags = ConcurrentHashMap<String, Tag>()
val fileController = FileController(files, tags)
val tagController = TagController(files, tags)
val db = DatabaseController()
val server = ServerController()

fun main() {
    server.create().start(wait = true)
}
