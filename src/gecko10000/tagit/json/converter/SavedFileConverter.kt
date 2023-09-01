package gecko10000.tagit.json.converter

import com.google.common.base.Converter
import gecko10000.tagit.json.enums.MediaType
import gecko10000.tagit.json.objects.SavedFile
import gecko10000.tagit.model.SavedFileEntity
import java.nio.file.Files

class SavedFileConverter(private val mediaTypeConverter: MediaTypeConverter) : Converter<SavedFileEntity, SavedFile>() {
    override fun doForward(entity: SavedFileEntity): SavedFile {
        val file = entity.file
        // probeContentType is better than using LocalFileContent
        // because LFC just looks at the extension
        val mimeType = Files.probeContentType(file.toPath())
        // elvis operator because probeContentType can return null
        val mediaType = mediaTypeConverter.convert(mimeType) ?: MediaType.UNKNOWN
        return SavedFile(
            file.name,
            mediaType,
            file.lastModified(),
            file.length(),
            null,
            null,
            entity.tags.map { it.fullName() }.toSet()
        )
    }

    override fun doBackward(file: SavedFile): SavedFileEntity {
        TODO("Not yet implemented")
    }
}
