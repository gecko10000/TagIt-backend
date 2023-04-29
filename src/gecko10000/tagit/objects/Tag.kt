package gecko10000.tagit.objects

import gecko10000.tagit.misc.tagDirectory
import gecko10000.tagit.serializers.SavedFileStringSerializer
import gecko10000.tagit.serializers.TagNameSerializer
import gecko10000.tagit.serializers.TagStringSerializer
import kotlinx.serialization.Serializable
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

@Serializable
data class Tag(
    val name: String,
    @Serializable(with = TagStringSerializer::class)
    val parent: Tag? = null,

    val children: MutableSet<@Serializable(with = TagNameSerializer::class) Tag> = ConcurrentSkipListSet(compareBy { it.name }),

    val files: MutableSet<@Serializable(with = SavedFileStringSerializer::class) SavedFile> = ConcurrentSkipListSet(compareBy { it.file.name })
) : Nameable() {

    fun fullName(): String = if (parent == null) name else parent.fullName() + "/" + name
    fun getDirectory() = File(tagDirectory + fullName())

    override fun name(): String = fullName()

    override fun hashCode() = fullName().hashCode()

}
