package gecko10000.tagit.json.mapper

import gecko10000.tagit.controller.DimensionsController
import gecko10000.tagit.json.`object`.JsonSavedFile
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.tagController
import gecko10000.tagit.thumbnailController
import java.util.function.Function

class SavedFileMapper(
    private val dimensionsMapper: DimensionsMapper,
    private val dimensionsController: DimensionsController,
) : Function<SavedFile, JsonSavedFile> {
    override fun apply(savedFile: SavedFile): JsonSavedFile {
        val file = savedFile.file
        // probeContentType is better than using LocalFileContent
        // because LFC just looks at the extension
        return JsonSavedFile(
            savedFile.uuid,
            file.name,
            savedFile.mediaType,
            file.lastModified(),
            file.length(),
            thumbnailController.getThumbnail(savedFile) != null,
            dimensionsController.getDimensions(savedFile)?.let { dimensionsMapper.apply(it) },
            savedFile.tags.mapNotNull { tagController[it] }.map { JsonMapper.CHILD_TAG.apply(it) }.toSet()
        )
    }
}
