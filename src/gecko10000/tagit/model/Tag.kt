package gecko10000.tagit.model

data class Tag(
    val name: String,
    val parent: Tag? = null,
    val children: Set<Tag> = setOf(),
    val files: Set<SavedFile> = setOf(),
) {

    fun fullName(): String = if (parent == null) name else parent.fullName() + "/" + name

    fun getAllFiles(): Set<SavedFile> {
        val childFiles = children.flatMap { it.getAllFiles() }
        return setOf(*files.toTypedArray(), *childFiles.toTypedArray())
    }

    override fun hashCode() = fullName().hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tag

        return fullName() == other.fullName()
    }

}
