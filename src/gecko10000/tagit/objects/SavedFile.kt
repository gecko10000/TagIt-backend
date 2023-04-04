package gecko10000.tagit.objects

import gecko10000.tagit.serializers.FileSerializer
import gecko10000.tagit.serializers.TagStringSerializer
import io.ktor.util.collections.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class SavedFile(
    @Serializable(with = FileSerializer::class)
    val file: File,
    val tags: MutableSet<@Serializable(with = TagStringSerializer::class) Tag> = ConcurrentSet()
    ) {
    /*fun addTags(vararg newTags: Tag) = SavedFile(file, buildSet(tags.size + newTags.size) {
        addAll(tags)
        addAll(newTags.map{ it.name })
    })*/
}
