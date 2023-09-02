package gecko10000.tagit

import gecko10000.tagit.misc.DataDirectory
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import java.io.File
import java.nio.file.Files

class FileManager {

    private fun loadFiles() {
        for (file in DataDirectory.FILE.listFiles()!!) {
            savedFiles[file.name] = SavedFile(file)
        }
    }

    private fun loadTagsRecursively(file: File, parent: Tag?) {
        if (!file.isDirectory) {
            val savedFile = savedFiles[file.name]
            savedFile ?: run {
                file.delete()
                return
            }
            addTags(savedFile, parent!!, link = false)
            return
        }
        // file is a directory, create tag and call recursively
        val tag = Tag(file.name, parent)
        tags[tag.fullName()] = tag
        parent?.children?.add(tag)
        for (f in file.listFiles()!!) {
            loadTagsRecursively(f, tag)
        }
    }

    private fun loadTags() {
        for (tagDir in DataDirectory.TAG.listFiles()!!) {
            loadTagsRecursively(tagDir, null)
        }
    }

    private fun createLink(tagDir: File, file: File) {
        try {
            Files.createLink(tagDir.resolve(file.name).toPath(), file.toPath())
        } catch (ex: Throwable) {
            println("createLink throwable: $ex")
        }
    }

    fun addTags(savedFile: SavedFile, vararg toAdd: Tag, link: Boolean = true) {
        savedFiles[savedFile.file.name] = SavedFile(savedFile.file, buildSet {
            addAll(savedFile.tags)
            addAll(toAdd)
        })
        for (tag in toAdd) {
            tag.files.add(savedFile)
            if (link) {
                val tagDir = DataDirectory.getTagDirectory(tag)
                createLink(tagDir, savedFile.file)
            }
        }
    }

    fun removeTags(savedFile: SavedFile, vararg toRemove: Tag) {
        savedFiles[savedFile.file.name] = SavedFile(savedFile.file, buildSet {
            addAll(savedFile.tags)
            removeAll(toRemove.toSet())
        })
        for (tag in toRemove) {
            tag.files.remove(savedFile)
            DataDirectory.getTagDirectory(tag).resolve(savedFile.file.name).delete()
        }
    }

    fun createTag(name: String): Tag? {
        // return existing
        tags[name]?.let { return it }
        val slashIndex = name.indexOfLast { c -> c == '/' }
        // create tags recursively
        val parent = if (slashIndex == -1) null else createTag(name.substring(0, slashIndex))
        val tag = Tag(name.substring(slashIndex + 1), parent)
        if (!DataDirectory.getTagDirectory(tag).mkdirs()) return null
        parent?.children?.add(tag)
        tags[name] = tag
        return tag
    }

    // create new tag
    // move files to tag
    // renameTag on children
    // delete tag from map
    fun renameTag(tag: Tag, newName: String): Boolean {
        val newTag = createTag(newName) ?: return false
        for (file in tag.files) {
            removeTags(file, tag)
            addTags(file, newTag)
        }
        var ok = true
        for (child in tag.children) {
            ok = ok and renameTag(child, "$newName/${child.name}")
        }
        deleteTag(tag)
        return ok
    }

    fun deleteTag(tag: Tag) {
        tag.parent?.children?.remove(tag)
        tags.remove(tag.fullName())
        DataDirectory.getTagDirectory(tag).deleteRecursively()
    }

    init {
        loadFiles()
        loadTags()
    }
}
