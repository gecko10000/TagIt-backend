package gecko10000.tagit.objects

import gecko10000.tagit.serializers.FileSerializer
import gecko10000.tagit.serializers.TagStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

@Serializable
data class SavedFile(
    @SerialName("name")
    @Serializable(with = FileSerializer::class)
    val file: File,

    val tags: MutableSet<@Serializable(with = TagStringSerializer::class) Tag> = ConcurrentSkipListSet(compareBy { it.name })
    ) : Nameable() {
    /*fun addTags(vararg newTags: Tag) = SavedFile(file, buildSet(tags.size + newTags.size) {
        addAll(tags)
        addAll(newTags.map{ it.name })
    })*/
    override fun name(): String = file.name
}
