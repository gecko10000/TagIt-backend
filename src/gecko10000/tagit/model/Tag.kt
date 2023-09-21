package gecko10000.tagit.model

import gecko10000.tagit.dataDirectory
import gecko10000.tagit.fileController
import gecko10000.tagit.misc.extension.getUUID
import gecko10000.tagit.tagController
import java.util.*

data class Tag(
    val name: String,
    val parent: UUID? = null,
    val children: Set<UUID> = setOf(),
    val files: Set<UUID> = setOf(),
) {
    val uuid: UUID = run {
        val dir = dataDirectory.getTagDirectory(this)
        dir.mkdirs()
        dir.getUUID()
    }

    fun fullName(): String = parent?.let { tagController[it] }?.let { "${it.fullName()}/$name" } ?: name

    fun getAllFiles(): Set<SavedFile> {
        val childFiles = children.flatMap { tagController[it]?.getAllFiles() ?: setOf() }
        val savedFiles = files.mapNotNull { fileController[it] }
        return savedFiles.plus(childFiles).toSet()
    }

    fun getAllChildren(): Set<Tag> {
        val indirectChildren = children.flatMap { tagController[it]?.getAllChildren() ?: setOf() }
        val childTags = children.mapNotNull { tagController[it] }
        return childTags.plus(indirectChildren).toSet()
    }

    override fun hashCode() = fullName().hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tag

        return fullName() == other.fullName()
    }

}
