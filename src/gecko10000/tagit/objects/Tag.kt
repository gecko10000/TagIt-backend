package gecko10000.tagit.objects

import gecko10000.tagit.tags
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Tag(
    val name: String,
    val parent: String? = null,
    val subTags: Set<String> = setOf(),
    val files: Set<String> = setOf()) {

    /*fun addSubTags(vararg tags: Tag) = Tag(name, parent, buildSet(subTags.size + tags.size) {
        addAll(subTags)
        addAll(tags.map { it.name })
    }, files)

    fun addFiles(vararg newFiles: SavedFile) = Tag(name, parent, subTags, buildSet(files.size + newFiles.size) {
        addAll(files)
        addAll(newFiles.map { it.file.name })
    })*/

    fun fullName() = if (parent == null) name else tags[parent].toString() + "/" + name
    fun getDirectory() = File("tags/${fullName()}")

}
