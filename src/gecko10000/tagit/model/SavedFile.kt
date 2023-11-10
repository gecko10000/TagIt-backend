package gecko10000.tagit.model

import gecko10000.tagit.misc.extension.getUUID
import gecko10000.tagit.model.enum.MediaType
import gecko10000.tagit.model.mapper.ModelMapper
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.URLConnection
import java.nio.file.Files
import java.util.*

data class SavedFile(
    val file: File,
    val tags: Set<UUID> = setOf(),
) {
    val uuid: UUID = file.getUUID()

    // don't want to just check the extension
    val mimeType: String? = run {
        val inputStream = BufferedInputStream(FileInputStream(file))
        val streamType = URLConnection.guessContentTypeFromStream(inputStream)
        streamType ?: Files.probeContentType(file.toPath())
    }

    //val mimeType: String? = Files.probeContentType(file.toPath())
    val mediaType = run {
        println(mimeType)
        mimeType?.let { ModelMapper.MEDIA_TYPE.apply(it) } ?: MediaType.UNKNOWN
    }
}
