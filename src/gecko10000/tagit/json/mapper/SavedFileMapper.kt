package gecko10000.tagit.json.mapper

import gecko10000.tagit.controller.DimensionsController
import gecko10000.tagit.json.`object`.JsonSavedFile
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.enum.TagOrder
import gecko10000.tagit.tagController
import gecko10000.tagit.thumbnailController

class SavedFileMapper(
    private val dimensionsMapper: DimensionsMapper,
    private val dimensionsController: DimensionsController,
) : (SavedFile, TagOrder, Boolean) -> JsonSavedFile {

    override fun invoke(savedFile: SavedFile, tagOrder: TagOrder, tagReversed: Boolean): JsonSavedFile {
        val file = savedFile.file
        val tags = savedFile.tags.mapNotNull { tagController[it] }
            .sortedWith(tagOrder.comparator)
            .let { if (tagReversed) it.reversed() else it }
            .map { JsonMapper.CHILD_TAG(it) }
        return JsonSavedFile(
            savedFile.uuid,
            file.name,
            savedFile.mediaType,
            file.lastModified(),
            file.length(),
            thumbnailController.getThumbnail(savedFile) != null,
            dimensionsController.getDimensions(savedFile)?.let { dimensionsMapper(it) },
            tags
        )
    }
}
