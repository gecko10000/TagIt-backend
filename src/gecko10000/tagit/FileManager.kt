package gecko10000.tagit

import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

class FileManager {

    private fun loadFiles() {
        val filesDir = File("files")
        if (filesDir.exists() && !filesDir.isDirectory) exitProcess(1) // files exists and is not a directory
        filesDir.mkdir()
        for (file in filesDir.listFiles()!!) {
            savedFiles[file.name] = SavedFile(file)
        }
    }

    private fun loadTagsRecursively(file: File, parent: Tag?) {
        if (!file.isDirectory) {
            val savedFile = savedFiles[file.name]
            // file no longer exists in files/ so there's no need to keep the symlink around
            savedFile ?: run {
                file.delete()
                return
            }
            addTags(savedFile, parent!!, link = false)
            return
        }
        // file is a directory, create tag and call recursively
        val tag = Tag(file.name, parent?.name)
        tags[tag.fullName()] = tag
        parent?.run { tags[parent.fullName()] = parent.copy(subTags = subTags.plus(tag.name)) }
        for (f in file.listFiles()!!) {
            loadTagsRecursively(f, tag)
        }
    }

    private fun loadTags() {
        val tagsDir = File("tags")
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
            println(ex)
        }
    }

    fun addTags(savedFile: SavedFile, vararg toAdd: Tag, link: Boolean = true) {
        val name = savedFile.file.name
        savedFiles[name] = savedFile.copy(tags = savedFile.tags.plus(toAdd.map { it.fullName() }))
        for (tag in toAdd) {
            tags[tag.fullName()] = tag.copy(files = tag.files.plus(name))
            val tagDir = tag.getDirectory()
            if (link) createLink(tagDir, savedFile.file)
        }
    }

    fun removeTags(savedFile: SavedFile, vararg toRemove: Tag) {
        val name = savedFile.file.name
        savedFiles[name] = savedFile.copy(tags = savedFile.tags.minus(toRemove.map { it.fullName() }.toSet()))
        for (tag in toRemove) {
            tags[tag.fullName()] = tag.copy(files = tag.files.minus(name))
            tag.getDirectory().resolve(savedFile.file.name).delete()
        }
    }

    init {
        loadFiles()
        loadTags()
    }
}
