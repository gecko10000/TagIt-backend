package gecko10000.tagit.json.converter

import com.google.common.base.Converter
import gecko10000.tagit.json.objects.Tag
import gecko10000.tagit.model.TagEntity

class TagConverter : Converter<TagEntity, Tag>() {

    private fun countChildren(entity: TagEntity): Int {
        return entity.files.size + entity.children.fold(0) { acc, it ->
            acc + countChildren(it)
        }
    }

    override fun doForward(entity: TagEntity): Tag {
        return Tag(
            entity.name,
            entity.parent?.fullName(),
            entity.children.map { it.name }.toSortedSet(),
            entity.files.map { it.file.name }.toSet(),
            countChildren(entity)
        )
    }

    override fun doBackward(tag: Tag): TagEntity {
        TODO("Not yet implemented")
    }
}
