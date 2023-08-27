package gecko10000.tagit.objects

import gecko10000.tagit.serializers.FileInfoSerializer
import gecko10000.tagit.serializers.TagStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

@Serializable
data class SavedFile(
    @Serializable(with = FileInfoSerializer::class)
    @SerialName("fileInfo")
    val file: File,
    val tags: MutableSet<@Serializable(with = TagStringSerializer::class) Tag> = ConcurrentSkipListSet(compareBy { it.name })
)
