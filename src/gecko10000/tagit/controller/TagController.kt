package gecko10000.tagit.controller

import gecko10000.tagit.dataDirectory
import gecko10000.tagit.fileController
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TagController(
    private val files: ConcurrentHashMap<UUID, SavedFile>,
    private val tags: ConcurrentHashMap<UUID, Tag>,
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun createTag(name: String): Tag {
        val existing = tags.values.firstOrNull { it.fullName() == name }
        existing?.run { return existing }

        log.info("Creating tag {}.", name)
        val slashIndex = name.indexOfLast { it == '/' }
        // create tags recursively
        val parent = if (slashIndex == -1) {
            null
        } else {
            createTag(name.substring(0, slashIndex))
        }
        val tag = Tag(name = name.substring(slashIndex + 1), parent = parent?.uuid)
        if (parent != null) {
            tags[parent.uuid] = parent.copy(children = parent.children.plus(tag.uuid))
        }
        tags[tag.uuid] = tag
        return tag
    }

    fun renameTag(tag: Tag, newName: String): Boolean {
        log.info("Renaming tag {} tag {}.", tag.fullName(), newName)
        // TODO: make this use the same directory to maintain UUID.
        val newTag = createTag(newName)
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

    operator fun get(uuid: UUID?): Tag? {
        uuid ?: return null
        return tags[uuid]
    }

    fun readOnlyTagMap(): Map<UUID, Tag> {
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
        for (fileId in tag.files) {
            val file = files[fileId] ?: continue
            fileController.removeTag(file, tag)
        }
        // remove self from parent tag
        val parent = tag.parent?.let { tags[it] }
        if (parent != null) {
            tags[parent.uuid] = parent.copy(children = parent.children.minus(tag.uuid))
        }
        // kys
        tags.remove(tag.uuid)
    }

    private fun loadFileTag(file: File, tag: Tag) {
        val name = file.name
        val savedFile = files.values.firstOrNull { it.file.name == name }
        // if the file is in the tags but not
        // found in saved files, we clean it up
        savedFile ?: run {
            file.delete()
            return
        }
        // TODO: change controller functions to retrieve latest values locally.
        val latestTag = tags[tag.uuid] ?: return
        fileController.addTagInternal(savedFile, latestTag)
    }

    private fun loadTagsRecursively(file: File, parent: Tag? = null) {
        if (!file.isDirectory) {
            // we have a file at the top level, ignore it
            // e.g. data/tags/hello.txt
            if (parent == null) return
            loadFileTag(file, parent)
            return
        }
        // directory, must be a tag
        val tag = Tag(name = file.name, parent = parent?.uuid)
        tags[tag.uuid] = tag
        if (parent != null) {
            val latestParent = tags[parent.uuid] ?: return
            tags[parent.uuid] = latestParent.copy(children = latestParent.children.plus(tag.uuid))
        }
        for (child in file.listFiles()!!) {
            loadTagsRecursively(child, tag)
        }
    }

    fun loadTags() {
        for (tagDir in dataDirectory.tag.listFiles()!!) {
            loadTagsRecursively(tagDir)
        }
    }
}
