package gecko10000.tagit.misc

import gecko10000.tagit.config
import gecko10000.tagit.model.Tag
import java.io.File

class DataDirectory {
    val base = config.dataDirectory

    val file = base.resolve("files")
    val tag = base.resolve("tags")
    val thumbnail = base.resolve("thumbnails")

    init {
        file.mkdirs()
        tag.mkdirs()
        thumbnail.mkdirs()
    }

    fun getTagDirectory(tag: Tag): File {
        return this.tag.resolve(tag.fullName())
    }
}
