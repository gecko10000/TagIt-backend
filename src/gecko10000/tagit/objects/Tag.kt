package gecko10000.tagit.objects

import gecko10000.tagit.misc.tagDirectory
import gecko10000.tagit.serializers.SavedFileStringSerializer
import gecko10000.tagit.serializers.TagNameSerializer
import gecko10000.tagit.serializers.TagStringSerializer
import io.ktor.util.collections.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Tag(
    val name: String,
    @Serializable(with = TagStringSerializer::class)
    val parent: Tag? = null,
    val children: MutableSet<@Serializable(with = TagNameSerializer::class) Tag> = ConcurrentSet(),
    val files: MutableSet<@Serializable(with = SavedFileStringSerializer::class) SavedFile> = ConcurrentSet()
) {

    fun fullName(): String = if (parent == null) name else parent.fullName() + "/" + name
    fun getDirectory() = File(tagDirectory + fullName())

}
