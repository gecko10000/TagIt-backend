package gecko10000.tagit.controller

import gecko10000.tagit.misc.DataDirectory
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class TagController(
    private val dataDirectory: DataDirectory,
    private val fileController: FileController,
    private val files: ConcurrentHashMap<String, SavedFile>,
    private val tags: ConcurrentHashMap<String, Tag>,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun createTag(name: String): Tag? {
        tags[name]?.let { return it }
        val slashIndex = name.indexOfLast { it == '/' }
        // create tags recursively
        val parent = if (slashIndex == -1) {
            null
        } else {
            createTag(name.substring(0, slashIndex))
        }
        val tag = Tag(name.substring(slashIndex + 1), parent)
        val tagDirectory = dataDirectory.getTagDirectory(tag)
        if (!tagDirectory.mkdirs()) {
            logger.error("Couldn't create directories for tag {}.", tag.fullName())
            return null
        }
        if (!dataDirectory.getTagDirectory(tag).mkdirs()) return null
        if (parent != null) {
            tags[parent.fullName()] = parent.copy(children = parent.children.plus(tag))
        }
        tags[name] = tag
        return tag
    }

    fun renameTag(tag: Tag, newName: String): Boolean {
        val newTag = createTag(newName) ?: return false
        for (file in tag.files) {
            fileController.removeTag(file, tag)
            fileController.addNewTag(file, newTag)
        }
        val success = tag.children.fold(true) { acc, child ->
            return acc and renameTag(child, "$newName/${child.name}")
        }
        deleteTag(tag)
        return success
    }

    fun deleteTag(tag: Tag) {
        for (child in tag.children) {
            deleteTag(child)
        }
        for (file in tag.files) {
            fileController.removeTag(file, tag)
        }
        val parent = tag.parent
        if (parent != null) {
            tags[parent.fullName()] = parent.copy(children = parent.children.minus(tag))
        }
        tags.remove(tag.fullName())
    }

    private fun loadFileTag(file: File, tag: Tag) {
        val name = file.name
        val savedFile = files[name]
        // if the file is in the tags but not
        // found in saved files, we clean it up
        savedFile ?: run {
            file.delete()
            return
        }
        fileController.addTag(savedFile, tag)
    }

    private fun loadTagsRecursively(file: File, parent: Tag? = null) {
        if (!file.isDirectory) {
            // we have a file at the top level, ignore it
            // e.g. tags/hello.txt
            if (parent == null) return
            loadFileTag(file, parent)
            return
        }
        // directory, must be a tag
        val tag = Tag(file.name, parent)
        tags[tag.fullName()] = tag
        if (parent != null) {
            tags[parent.fullName()] = parent.copy(children = parent.children.plus(tag))
        }
        for (child in file.listFiles()!!) {
            loadTagsRecursively(child, tag)
        }
    }

    private fun loadTags() {
        for (tagDir in dataDirectory.tag.listFiles()!!) {
            loadTagsRecursively(tagDir)
        }
    }

    init {
        loadTags()
    }
}
