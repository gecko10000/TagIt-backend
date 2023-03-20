package gecko10000.tagit.objects

import gecko10000.tagit.serializers.FileSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

@Serializable
data class SavedFile(
    @Serializable(with = FileSerializer::class)
    val file: File,
    @Transient
    val tags: Set<String> = setOf()
    ) {


    /*fun addTags(vararg newTags: Tag) = SavedFile(file, buildSet(tags.size + newTags.size) {
        addAll(tags)
        addAll(newTags.map{ it.name })
    })*/
}
