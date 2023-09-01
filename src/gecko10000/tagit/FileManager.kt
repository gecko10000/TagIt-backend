package gecko10000.tagit

import gecko10000.tagit.misc.fileDirectory
import gecko10000.tagit.misc.tagDirectory
import gecko10000.tagit.misc.thumbnailDirectory
import gecko10000.tagit.model.SavedFileEntity
import gecko10000.tagit.model.TagEntity
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

class FileManager {

    private fun loadFiles() {
        val filesDir = File(fileDirectory)
        if (filesDir.exists() && !filesDir.isDirectory) exitProcess(1) // files exists and is not a directory
        filesDir.mkdir()
        for (file in filesDir.listFiles()!!) {
            savedFiles[file.name] = SavedFileEntity(file)
        }
    }

    private fun loadTagsRecursively(file: File, parent: TagEntity?) {
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
        val tag = TagEntity(file.name, parent)
        tags[tag.fullName()] = tag
        parent?.children?.add(tag)
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

    fun addTags(savedFile: SavedFileEntity, vararg toAdd: TagEntity, link: Boolean = true) {
        savedFiles[savedFile.file.name] = SavedFileEntity(savedFile.file, buildSet {
            addAll(savedFile.tags)
            addAll(toAdd)
        })
        for (tag in toAdd) {
            tag.files.add(savedFile)
            if (link) {
                val tagDir = tag.getDirectory()
                createLink(tagDir, savedFile.file)
            }
        }
    }

    fun removeTags(savedFile: SavedFileEntity, vararg toRemove: TagEntity) {
        savedFiles[savedFile.file.name] = SavedFileEntity(savedFile.file, buildSet {
            addAll(savedFile.tags)
            removeAll(toRemove.toSet())
        })
        for (tag in toRemove) {
            tag.files.remove(savedFile)
            tag.getDirectory().resolve(savedFile.file.name).delete()
        }
    }

    fun createTag(name: String): TagEntity? {
        // return existing
        tags[name]?.let { return it }
        val slashIndex = name.indexOfLast { c -> c == '/' }
        // create tags recursively
        val parent = if (slashIndex == -1) null else createTag(name.substring(0, slashIndex))
        val tag = TagEntity(name.substring(slashIndex + 1), parent)
        if (!tag.getDirectory().mkdirs()) return null
        parent?.children?.add(tag)
        tags[name] = tag
        return tag
    }

    // create new tag
    // move files to tag
    // renameTag on children
    // delete tag from map
    fun renameTag(tag: TagEntity, newName: String): Boolean {
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

    fun deleteTag(tag: TagEntity) {
        tag.parent?.children?.remove(tag)
        tags.remove(tag.fullName())
        tag.getDirectory().deleteRecursively()
    }

    init {
        File(thumbnailDirectory).mkdir()
        loadFiles()
        loadTags()
    }
}
