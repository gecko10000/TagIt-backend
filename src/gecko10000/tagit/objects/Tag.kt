package gecko10000.tagit.objects

import gecko10000.tagit.HexColor

data class Tag(val name: String, val color: HexColor, val subtags: Set<Tag>, val files: Set<SavedFile>, val parent: Tag?)
