package gecko10000.tagit.objects

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.concurrent.ConcurrentSkipListSet

@Serializable
data class Tag(val name: String, val parent: Tag? = null) {
    @Transient
    val subtags: MutableSet<Tag> = ConcurrentSkipListSet()

    @Transient
    val files: MutableSet<SavedFile> = ConcurrentSkipListSet()
}
