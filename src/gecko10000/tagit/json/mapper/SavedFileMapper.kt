package gecko10000.tagit.json.mapper

import gecko10000.tagit.json.enum.MediaType
import gecko10000.tagit.json.`object`.JsonSavedFile
import gecko10000.tagit.model.SavedFile
import java.nio.file.Files
import java.util.function.Function

class SavedFileMapper(private val mediaTypeMapper: MediaTypeMapper) : Function<SavedFile, JsonSavedFile> {
    override fun apply(savedFile: SavedFile): JsonSavedFile {
        val file = savedFile.file
        // probeContentType is better than using LocalFileContent
        // because LFC just looks at the extension
        val mimeType = Files.probeContentType(file.toPath())
        val mediaType = mimeType?.let { mediaTypeMapper.apply(it) } ?: MediaType.UNKNOWN
        return JsonSavedFile(
            file.name,
            mediaType,
            file.lastModified(),
            file.length(),
            null,
            null,
            savedFile.tags
        )
    }
}
