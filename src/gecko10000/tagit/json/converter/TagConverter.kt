package gecko10000.tagit.json.converter

import com.google.common.base.Converter
import gecko10000.tagit.json.objects.Tag
import gecko10000.tagit.model.TagEntity

class TagConverter : Converter<TagEntity, Tag>() {

    override fun doForward(entity: TagEntity): Tag {
        return Tag(
            entity.name,
            entity.parent?.fullName(),
            entity.children.map { it.name }.toSortedSet(),
            entity.files.map { it.file.name }.toSet(),
            entity.getAllFiles().size
        )
    }

    override fun doBackward(tag: Tag): TagEntity {
        TODO("Not yet implemented")
    }
}
