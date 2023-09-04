package gecko10000.tagit.controller

import gecko10000.tagit.dataDirectory
import gecko10000.tagit.fileController
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class TagController(
    private val files: ConcurrentHashMap<String, SavedFile>,
    private val tags: ConcurrentHashMap<String, Tag>,
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun createTag(name: String): Tag? {
        tags[name]?.let { return it }
        log.info("Creating tag {}.", name)
        val slashIndex = name.indexOfLast { it == '/' }
        // create tags recursively
        val parent = if (slashIndex == -1) {
            null
        } else {
            createTag(name.substring(0, slashIndex))
        }
        val tag = Tag(name.substring(slashIndex + 1), parent?.fullName())
        val tagDirectory = dataDirectory.getTagDirectory(tag)
        if (!tagDirectory.mkdirs()) {
            log.error("Couldn't create directories for tag {}.", tag.fullName())
            return null
        }
        if (!dataDirectory.getTagDirectory(tag).mkdirs()) return null
        if (parent != null) {
            tags[parent.fullName()] = parent.copy(children = parent.children.plus(tag.fullName()))
        }
        tags[name] = tag
        return tag
    }

    fun renameTag(tag: Tag, newName: String): Boolean {
        log.info("Renaming tag {} tag {}.", tag.fullName(), newName)
        val newTag = createTag(newName) ?: return false
        for (fileName in tag.files) {
            val file = files[fileName] ?: continue
            fileController.removeTag(file, tag)
            fileController.addTag(file, newTag)
        }
        val success = tag.children.fold(true) { acc, childName ->
            val child = tags[childName] ?: return@fold false
            return acc and renameTag(child, "$newName/${childName}")
        }
        deleteTag(tag)
        return success
    }

    operator fun get(fullName: String?): Tag? {
        return tags[fullName]
    }

    fun readOnlyTagMap(): Map<String, Tag> {
        return tags.toMap()
    }

    fun deleteTag(tag: Tag) {
        log.info("Deleting tag {}.", tag.fullName())
        // delete children tags
        for (childName in tag.children) {
            val child = tags[childName] ?: continue
            deleteTag(child)
        }
        // remove tag from files
        for (fileName in tag.files) {
            val file = files[fileName] ?: continue
            fileController.removeTag(file, tag)
        }
        // remove self from parent tag
        val parent = tags[tag.parent]
        if (parent != null) {
            tags[parent.fullName()] = parent.copy(children = parent.children.minus(tag.fullName()))
        }
        // kys
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
        val latestTag = tags[tag.fullName()] ?: return
        fileController.addTagInternal(savedFile, latestTag)
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
        val parentName = parent?.fullName()
        val tag = Tag(file.name, parentName)
        tags[tag.fullName()] = tag
        if (parentName != null) {
            val latestParent = tags[parentName] ?: return
            tags[parentName] = latestParent.copy(children = latestParent.children.plus(tag.fullName()))
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

    // NOTE: DO NOT USE the tagController in any child functions
    // possible TODO: move tag loading outside of init so we can use it?
    init {
        loadTags()
    }
}
