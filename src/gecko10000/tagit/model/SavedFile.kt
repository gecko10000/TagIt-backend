package gecko10000.tagit.model

import gecko10000.tagit.misc.extension.getUUID
import gecko10000.tagit.model.enum.MediaType
import gecko10000.tagit.model.mapper.ModelMapper
import java.io.File
import java.nio.file.Files
import java.util.*

data class SavedFile(
    val file: File,
    val tags: Set<UUID> = setOf(),
) {
    val uuid: UUID = file.getUUID()

    // probeContentType is better than using LocalFileContent
    // because LFC just looks at the extension
    val mimeType: String? = Files.probeContentType(file.toPath())
    val mediaType = run {
        mimeType?.let { ModelMapper.MEDIA_TYPE.apply(it) } ?: MediaType.UNKNOWN
    }
}
