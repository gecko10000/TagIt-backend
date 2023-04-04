package gecko10000.tagit.objects

import gecko10000.tagit.serializers.SavedFileStringSerializer
import gecko10000.tagit.serializers.TagStringSerializer
import gecko10000.tagit.misc.tagDirectory
import io.ktor.util.collections.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Tag(
    val name: String,
    @Serializable(with = TagStringSerializer::class)
    val parent: Tag? = null,
    val subTags: MutableSet<@Serializable(with = TagStringSerializer::class) Tag> = ConcurrentSet(),
    val files: MutableSet<@Serializable(with = SavedFileStringSerializer::class) SavedFile> = ConcurrentSet()
) {

    /*fun addSubTags(vararg tags: Tag) = Tag(name, parent, buildSet(subTags.size + tags.size) {
        addAll(subTags)
        addAll(tags.map { it.name })
    }, files)

    fun addFiles(vararg newFiles: SavedFile) = Tag(name, parent, subTags, buildSet(files.size + newFiles.size) {
        addAll(files)
        addAll(newFiles.map { it.file.name })
    })*/

    fun fullName(): String = if (parent == null) name else parent.fullName() + "/" + name
    fun getDirectory() = File(tagDirectory + fullName())

}
