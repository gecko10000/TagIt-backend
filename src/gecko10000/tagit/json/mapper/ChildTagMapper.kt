package gecko10000.tagit.json.mapper

import gecko10000.tagit.json.`object`.JsonChildTag
import gecko10000.tagit.model.Tag
import java.util.function.Function

class ChildTagMapper : Function<Tag, JsonChildTag> {
    override fun apply(tag: Tag): JsonChildTag {
        return JsonChildTag(
            tag.name,
            tag.children.size,
            tag.getAllFiles().size
        )
    }
}
