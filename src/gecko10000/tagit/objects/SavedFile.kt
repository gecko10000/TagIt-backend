package gecko10000.tagit.objects

import gecko10000.tagit.serializers.FileSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

@Serializable
data class SavedFile(@Serializable(with = FileSerializer::class)
                     val file: File) {
    @Transient
    val tags: MutableSet<Tag> = mutableSetOf()
}
