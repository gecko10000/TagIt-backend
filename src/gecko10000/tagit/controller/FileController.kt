package gecko10000.tagit.controller

import gecko10000.tagit.misc.DataDirectory
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap

open class FileController(
    private val dataDirectory: DataDirectory,
    private val files: ConcurrentHashMap<String, SavedFile>,
    private val tags: ConcurrentHashMap<String, Tag>,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)


    private fun loadFiles() {
        for (file in dataDirectory.file.listFiles()!!) {
            files[file.name] = SavedFile(file)
        }
    }

    // this only adds the tag to the maps, it does not modify the filesystem.
    // to create the symlink, use `addNewTag` below.
    internal fun addTag(savedFile: SavedFile, tag: Tag) {
        files[savedFile.file.name] = savedFile.copy(tags = savedFile.tags.plus(tag))
        tags[tag.fullName()] = tag.copy(files = tag.files.plus(savedFile))
    }

    fun addNewTag(savedFile: SavedFile, tag: Tag) {
        addTag(savedFile, tag)
        val tagDir = dataDirectory.getTagDirectory(tag)
        createLink(tagDir, savedFile.file)
    }

    private fun createLink(tagDir: File, file: File) {
        try {
            Files.createLink(tagDir.resolve(file.name).toPath(), file.toPath())
        } catch (ex: Throwable) {
            logger.error("Couldn't link {} to tag {}: {}", file, tagDir.path, ex.message)
        }
    }

    fun removeTag(savedFile: SavedFile, tag: Tag) {
        val fileName = savedFile.file.name
        files[fileName] = savedFile.copy(tags = savedFile.tags.minus(tag))
        tags[tag.fullName()] = tag.copy(files = tag.files.minus(savedFile))
        dataDirectory.getTagDirectory(tag).resolve(fileName).delete()
    }

    init {
        loadFiles()
    }

}
