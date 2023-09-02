package gecko10000.tagit.json.mapper

import gecko10000.tagit.json.`object`.JsonTag
import gecko10000.tagit.model.Tag
import java.util.function.Function

class TagMapper(
    private val childTagMapper: ChildTagMapper,
    private val savedFileMapper: SavedFileMapper,
) : Function<Tag, JsonTag> {

    override fun apply(tag: Tag): JsonTag {
        return JsonTag(
            tag.name,
            tag.parent?.fullName(),
            tag.children.map { childTagMapper.apply(it) }.toSortedSet(compareBy { it.name }),
            tag.files.map { savedFileMapper.apply(it) }.toSet(),
            tag.getAllFiles().size
        )
    }
}
