package gecko10000.tagit.json.mapper

import gecko10000.tagit.json.`object`.JsonChildTag
import gecko10000.tagit.model.Tag
import gecko10000.tagit.tagController
import java.util.function.Function

class ChildTagMapper(private val tagCountsMapper: TagCountsMapper) : Function<Tag, JsonChildTag> {
    override fun apply(tag: Tag): JsonChildTag {
        return JsonChildTag(
            tag.uuid,
            tag.name,
            tag.parent,
            tagController[tag.parent]?.fullName(),
            tagCountsMapper.apply(tag),
        )
    }
}
