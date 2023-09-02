package gecko10000.tagit.controller

import gecko10000.tagit.dataDirectory
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

open class FileController(
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
        val oldFile = savedFile.file
        val newFile = oldFile.parentFile.resolve(newName)
        if (newFile.exists()) return call?.respond(HttpStatusCode.BadRequest, "New filename already exists.") ?: Unit
        oldFile.renameTo(newFile)
        val newSavedFile = savedFile.copy(file = newFile)
        files[newName] = newSavedFile
        for (tag in savedFile.tags) {
            val newTag = tag.copy(files = tag.files.minus(savedFile).plus(newSavedFile))
            tags[tag.fullName()] = newTag
        }
        files.remove(oldFile.name)
    }

    fun deleteFile(savedFile: SavedFile) {
        for (tag in savedFile.tags) {
            removeTag(savedFile, tag)
        }
        savedFile.file.delete()
        files.remove(savedFile.file.name)
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
            log.error("Couldn't link {} to tag {}: {}", file, tagDir.path, ex.message)
        }
    }

    fun removeTag(savedFile: SavedFile, tag: Tag) {
        val fileName = savedFile.file.name
        files[fileName] = savedFile.copy(tags = savedFile.tags.minus(tag))
        tags[tag.fullName()] = tag.copy(files = tag.files.minus(savedFile))
        dataDirectory.getTagDirectory(tag).resolve(fileName).delete()
    }

    private fun loadFiles() {
        for (file in dataDirectory.file.listFiles()!!) {
            files[file.name] = SavedFile(file)
        }
    }

    init {
        loadFiles()
    }

}
