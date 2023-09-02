package gecko10000.tagit.model

data class TagEntity(
    val name: String,
    val parent: TagEntity? = null,
    val children: Set<TagEntity> = setOf(),
    val files: Set<SavedFileEntity> = setOf(),
) {

    fun fullName(): String = if (parent == null) name else parent.fullName() + "/" + name

    override fun hashCode() = fullName().hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TagEntity

        return fullName() == other.fullName()
    }

}
