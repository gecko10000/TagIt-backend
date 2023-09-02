package gecko10000.tagit.json.converter

import com.google.common.base.Converter
import gecko10000.tagit.json.objects.JsonTag
import gecko10000.tagit.model.Tag

class TagConverter : Converter<Tag, JsonTag>() {

    override fun doForward(entity: Tag): JsonTag {
        return JsonTag(
            entity.name,
            entity.parent?.fullName(),
            entity.children.map { it.name }.toSortedSet(),
            entity.files.map { it.file.name }.toSet(),
            entity.getAllFiles().size
        )
    }

    override fun doBackward(jsonTag: JsonTag): Tag {
        TODO("Not yet implemented")
    }
}
