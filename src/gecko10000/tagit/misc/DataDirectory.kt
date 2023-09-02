package gecko10000.tagit.misc

import gecko10000.tagit.model.Tag
import java.io.File

class DataDirectory {
    companion object {
        private val BASE = File(Config.DATA_DIRECTORY)
        val FILE = BASE.resolve("files")
        val TAG = BASE.resolve("tags")
        val THUMBNAIL = BASE.resolve("thumbnails")

        fun getTagDirectory(tag: Tag): File {
            return DataDirectory.TAG.resolve(tag.fullName())
        }
    }
}
