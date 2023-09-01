package gecko10000.tagit.misc

import java.io.File

class DataDirectory {
    companion object {
        private val BASE = File(Config.DATA_DIRECTORY)
        val FILE = BASE.resolve("files")
        val TAG = BASE.resolve("tags")
        val THUMBNAIL = BASE.resolve("thumbnails")
    }
}
