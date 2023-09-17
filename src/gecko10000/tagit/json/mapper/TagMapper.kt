package gecko10000.tagit.json.mapper

import gecko10000.tagit.fileController
import gecko10000.tagit.json.`object`.JsonTag
import gecko10000.tagit.model.Tag
import gecko10000.tagit.tagController
import java.util.function.Function

class TagMapper(
    private val childTagMapper: ChildTagMapper,
    private val savedFileMapper: SavedFileMapper,
) : Function<Tag, JsonTag> {

    override fun apply(tag: Tag): JsonTag {
        return JsonTag(
            tag.uuid,
            tag.name,
            tag.parent,
            tagController[tag.parent]?.fullName(),
            tag.children.mapNotNull { tagController[it] }
                .map { childTagMapper.apply(it) }
                .toSortedSet(compareBy { it.name }),
            tag.files.mapNotNull { fileController[it] }
                .map { savedFileMapper.apply(it) }
                .toSet(),
            tag.getAllFiles().size
        )
    }
}
