package gecko10000.tagit.misc.extension

import org.apache.commons.io.file.PathUtils
import java.io.File
import java.util.*

fun File.getUUID(): UUID {
    val attributes = PathUtils.readBasicFileAttributes(this.toPath())
    val fileKeyString = attributes.fileKey().toString()
            return UUID.nameUUIDFromBytes(fileKeyString.toByteArray())
}
