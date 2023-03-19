package gecko10000.tagit.objects

import gecko10000.tagit.serializers.FileSerializer
import io.netty.util.internal.ConcurrentSet
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

@Serializable
data class SavedFile(@Serializable(with = FileSerializer::class)
                     val file: File) {
    @Transient
    val tags: MutableSet<Tag> = ConcurrentSkipListSet()
}
