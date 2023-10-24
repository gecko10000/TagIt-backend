package gecko10000.tagit.json.mapper

import gecko10000.tagit.json.`object`.JsonChildTag
import gecko10000.tagit.model.Tag
import gecko10000.tagit.tagController

class ChildTagMapper(private val tagCountsMapper: TagCountsMapper) : (Tag) -> JsonChildTag {
    override fun invoke(tag: Tag): JsonChildTag {
        return JsonChildTag(
            tag.uuid,
            tag.name,
            tag.parent,
            tagController[tag.parent]?.fullName(),
            tagCountsMapper(tag),
        )
    }
}
