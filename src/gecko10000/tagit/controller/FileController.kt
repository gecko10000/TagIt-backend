package gecko10000.tagit.controller

import gecko10000.tagit.dataDirectory
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import gecko10000.tagit.mutex
import gecko10000.tagit.tagController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class FileController(
    private val files: ConcurrentHashMap<String, SavedFile>,
    private val tags: ConcurrentHashMap<String, Tag>,
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    suspend fun addFile(inputStream: InputStream, name: String, call: ApplicationCall? = null) {
        val file = dataDirectory.file.resolve(name)
        try {
            val outputStream = file.outputStream()
            inputStream.transferTo(outputStream)
            outputStream.close()
        } catch (ex: IOException) {
            log.error("Could not save file {}.", name)
            file.delete()
            call?.respond(HttpStatusCode.InternalServerError, ex)
        }
        mutex.withLock {
            files[name] = SavedFile(file)
        }
    }

    operator fun get(name: String?): SavedFile? {
        return files[name]
    }

    fun readOnlyFileMap(): Map<String, SavedFile> {
        return files.toMap()
    }

    // mv old new
    // add new SavedFile
    // update tags with new SavedFile
    // remove old SavedFile
    suspend fun renameFile(savedFile: SavedFile, newName: String, call: ApplicationCall? = null) {
        log.info("Renaming file {} to {}.", savedFile.file.name, newName)
        val oldFile = savedFile.file
        val newFile = oldFile.parentFile.resolve(newName)
        if (newFile.exists()) return call?.respond(HttpStatusCode.BadRequest, "New filename already exists.") ?: Unit
        oldFile.renameTo(newFile)
        val newSavedFile = savedFile.copy(file = newFile)
        files[newName] = newSavedFile
        for (tagName in savedFile.tags) {
            val tag = tagController[tagName] ?: continue
            val newTag = tag.copy(files = tag.files.minus(oldFile.name).plus(newFile.name))
            tags[tag.fullName()] = newTag
        }
        files.remove(oldFile.name)
    }

    fun deleteFile(savedFile: SavedFile) {
        log.info("Deleting file {}.", savedFile.file.name)
        for (tagName in savedFile.tags) {
            val tag = tagController[tagName] ?: continue
            removeTag(savedFile, tag)
        }
        savedFile.file.delete()
        files.remove(savedFile.file.name)
    }

    // this only adds the tag to the maps, it does not modify the filesystem.
    // to create the symlink, use `addNewTag` below.
    fun addTagInternal(savedFile: SavedFile, tag: Tag) {
        files[savedFile.file.name] = savedFile.copy(tags = savedFile.tags.plus(tag.fullName()))
        tags[tag.fullName()] = tag.copy(files = tag.files.plus(savedFile.file.name))
    }

    fun addTag(savedFile: SavedFile, tag: Tag) {
        log.info("Adding tag {} to {}.", tag.fullName(), savedFile.file.name)
        addTagInternal(savedFile, tag)
        val tagDir = dataDirectory.getTagDirectory(tag)
        createLink(tagDir, savedFile.file)
    }

    private fun createLink(tagDir: File, file: File) {
        try {
            Files.createLink(tagDir.resolve(file.name).toPath(), file.toPath())
        } catch (ex: Throwable) {
            log.error("Couldn't link {} to tag {}: {}", file, tagDir.path, ex.message)
        }
    }

    fun removeTag(savedFile: SavedFile, tag: Tag) {
        val fileName = savedFile.file.name
        val tagName = tag.fullName()
        log.info("Removing tag {} from {}.", tagName, fileName)
        files[fileName] = savedFile.copy(tags = savedFile.tags.minus(tagName))
        tags[tagName] = tag.copy(files = tag.files.minus(fileName))
        dataDirectory.getTagDirectory(tag).resolve(fileName).delete()
    }

    private fun loadFiles() {
        val fileList = dataDirectory.file.listFiles()!!
        for (file in fileList) {
            files[file.name] = SavedFile(file)
        }
        log.info("Loaded {} files.", fileList.size)
    }

    init {
        loadFiles()
    }

}
