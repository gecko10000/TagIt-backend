package gecko10000.tagit.misc

import gecko10000.tagit.model.Tag
import java.io.File

class DataDirectory(private val config: Config) {
    private val base = File(config.dataDirectory)

    val file = base.resolve("files")
    val tag = base.resolve("tags")
    val thumbnail = base.resolve("thumbnails")

    fun getTagDirectory(tag: Tag): File {
        return this.tag.resolve(tag.fullName())
    }
}
