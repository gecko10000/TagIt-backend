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

fun main() {
    val config = Config()
    val dataDirectory = DataDirectory(config)
    val files = ConcurrentHashMap<String, SavedFile>()
    val tags = ConcurrentHashMap<String, Tag>()
    val fileController = FileController(dataDirectory, files, tags)
    val tagController = TagController(dataDirectory, fileController, files, tags)
    val db = DatabaseController()
    val server = ServerController(db, config)
    server.create().start(wait = true)
}
