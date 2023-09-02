package gecko10000.tagit

import gecko10000.tagit.controller.DatabaseController
import gecko10000.tagit.controller.FileController
import gecko10000.tagit.controller.ServerController
import gecko10000.tagit.controller.TagController
import gecko10000.tagit.misc.Config
import gecko10000.tagit.misc.DataDirectory
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import java.util.concurrent.ConcurrentHashMap

val config = Config()
val dataDirectory = DataDirectory()

// we make these private so they can't be modified externally
// as that would risk a break in the structure
private val files = ConcurrentHashMap<String, SavedFile>()
private val tags = ConcurrentHashMap<String, Tag>()
val fileController = FileController(files, tags)
val tagController = TagController(files, tags)
val db = DatabaseController()
val server = ServerController()

fun main() {
    server.create().start(wait = true)
}
