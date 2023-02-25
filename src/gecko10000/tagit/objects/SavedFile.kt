package gecko10000.tagit.objects

import java.io.File

data class SavedFile(val name: String, val file: File, val tags: Set<Tag>)
