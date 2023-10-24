package gecko10000.tagit.json.mapper

import gecko10000.tagit.json.`object`.JsonTagCounts
import gecko10000.tagit.model.Tag

class TagCountsMapper : (Tag) -> JsonTagCounts {

    override fun invoke(tag: Tag): JsonTagCounts {
        return JsonTagCounts(
            tag.children.size,
            tag.getAllChildren().size,
            tag.files.size,
            tag.getAllFiles().size,
        )
    }
}
