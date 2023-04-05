package gecko10000.tagit

import gecko10000.tagit.misc.fileDirectory
import gecko10000.tagit.misc.tagDirectory
import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

class FileManager {

    private fun loadFiles() {
        val filesDir = File(fileDirectory)
        if (filesDir.exists() && !filesDir.isDirectory) exitProcess(1) // files exists and is not a directory
        filesDir.mkdir()
        for (file in filesDir.listFiles()!!) {
            savedFiles[file.name] = SavedFile(file)
        }
    }

    private fun loadTagsRecursively(file: File, parent: Tag?) {
        if (!file.isDirectory) {
            val savedFile = savedFiles[file.name]
            // TODO: check if the file is actually hardlinked to the same name? what would even happen in that situation?
            // file no longer exists in files/ so there's no need to keep the symlink around
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
        parent?.subTags?.add(tag)
        for (f in file.listFiles()!!) {
            loadTagsRecursively(f, tag)
        }
    }

    private fun loadTags() {
        val tagsDir = File(tagDirectory)
        if (tagsDir.exists() && !tagsDir.isDirectory) exitProcess(2) // tags exists and is not a directory
        tagsDir.mkdir()
        for (tagDir in tagsDir.listFiles()!!) {
            if (!tagDir.isDirectory) exitProcess(3) // non-directory in tags/
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
        savedFile.tags.addAll(toAdd)
        for (tag in toAdd) {
            tag.files.add(savedFile)
            if (link) {
                val tagDir = tag.getDirectory()
                createLink(tagDir, savedFile.file)
            }
        }
    }

    fun removeTags(savedFile: SavedFile, vararg toRemove: Tag) {
        savedFile.tags.removeAll(toRemove.toSet())
        for (tag in toRemove) {
            tag.files.remove(savedFile)
            tag.getDirectory().resolve(savedFile.file.name).delete()
        }
    }

    fun createTag(name: String): Tag {
        // return existing
        tags[name]?.let { return it }
        val slashIndex = name.indexOfLast { c -> c == '/' }
        // create tags recursively
        val parent = if (slashIndex == -1) null else createTag(name.substring(0, slashIndex))
        val tag = Tag(name.substring(slashIndex + 1), parent)
        parent?.subTags?.add(tag)
        tags[name] = tag
        tag.getDirectory().mkdirs()
        return tag
    }

    // create new tag
    // move files to tag
    // renameTag on subTags
    // delete tag from map
    fun renameTag(tag: Tag, newName: String) {
        val newTag = createTag(newName)
        for (file in tag.files) {
            removeTags(file, tag)
            addTags(file, newTag)
        }
        for (subTag in tag.subTags) {
            renameTag(subTag, "$newName/${subTag.name}")
        }
        deleteTag(tag)
    }

    fun deleteTag(tag: Tag) {
        tag.parent?.subTags?.remove(tag)
        tags.remove(tag.fullName())
        tag.getDirectory().deleteRecursively()
    }

    init {
        loadFiles()
        loadTags()
    }
}
