package gecko10000.tagit.json.objects

data class Tag(
    val name: String,
    val parent: String?,
    val children: Set<String>,
    val files: Set<String>,
    val totalFiles: Int,
)
