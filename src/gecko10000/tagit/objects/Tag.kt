package gecko10000.tagit.objects

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Tag(val name: String, val parent: Tag? = null) {
    @Transient
    val subtags: Set<Tag> = mutableSetOf()

    @Transient
    val files: Set<SavedFile> = mutableSetOf()
}
