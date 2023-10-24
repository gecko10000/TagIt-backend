package gecko10000.tagit.json.mapper

import gecko10000.tagit.fileController
import gecko10000.tagit.json.`object`.JsonTag
import gecko10000.tagit.model.Tag
import gecko10000.tagit.model.enum.FileOrder
import gecko10000.tagit.model.enum.TagOrder
import gecko10000.tagit.tagController

class TagMapper(
    private val childTagMapper: ChildTagMapper,
    private val savedFileMapper: SavedFileMapper,
) : (Tag, TagOrder, Boolean, FileOrder, Boolean) -> JsonTag {

    override fun invoke(
        tag: Tag,
        tagOrder: TagOrder,
        tagReversed: Boolean,
        fileOrder: FileOrder,
        fileReversed: Boolean
    ): JsonTag {
        val children = tag.children.mapNotNull { tagController[it] }
            .sortedWith(tagOrder.comparator)
            .let { if (tagReversed) it.reversed() else it }
            .map { childTagMapper(it) }
        val files = tag.files.mapNotNull { fileController[it] }
            .sortedWith(fileOrder.comparator)
            .let { if (fileReversed) it.reversed() else it }
            .map { savedFileMapper(it, tagOrder, tagReversed) }
        return JsonTag(
            tag.uuid,
            tag.name,
            tag.parent,
            tagController[tag.parent]?.fullName(),
            children,
            files,
            tag.getAllFiles().size
        )
    }
}
