package gecko10000.tagit.controller

import gecko10000.tagit.dataDirectory
import gecko10000.tagit.fileController
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import gecko10000.tagit.mutex
import gecko10000.tagit.tagController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class FileController(
    private val files: ConcurrentHashMap<UUID, SavedFile>,
    private val tags: ConcurrentHashMap<UUID, Tag>,
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    suspend fun addFile(
        inputChannel: ByteReadChannel,
        expectedSize: Long,
        name: String
    ): SavedFile {
        val file = dataDirectory.file.resolve(name)
        file.createNewFile()
        val outputChannel = file.writeChannel()
        try {
            inputChannel.copyAndClose(outputChannel)
        } catch (ex: IOException) {
            log.error("Could not save file {}.", name)
            file.delete()
            throw ex
        }
        if (inputChannel.totalBytesRead != expectedSize) {
            log.error(
                "File upload for {} terminated early ({} instead of {} bytes).",
                name,
                inputChannel.totalBytesRead,
                expectedSize
            )
            file.delete()
            throw IOException("File upload terminated early.")
        }
        val savedFile = SavedFile(file = file)
        mutex.withLock {
            files[savedFile.uuid] = savedFile
        }
        return savedFile
    }

    operator fun get(uuid: UUID?): SavedFile? {
        uuid ?: return null
        return files[uuid]
    }

    fun readOnlyFileMap(): Map<UUID, SavedFile> {
        return files.toMap()
    }

    // changes the name for the existing savedFile
    suspend fun renameFile(savedFile: SavedFile, newName: String, call: ApplicationCall? = null) {
        val oldFile = savedFile.file
        val newFile = oldFile.parentFile.resolve(newName)
        if (newFile.exists()) return call?.respond(HttpStatusCode.BadRequest, "New filename already exists.") ?: Unit

        log.info("Renaming file {} to {}.", savedFile.file.name, newName)
        oldFile.renameTo(newFile)
        val newSavedFile = savedFile.copy(file = newFile)
        files[savedFile.uuid] = newSavedFile
        for (tagId in savedFile.tags) {
            val tag = tags[tagId] ?: continue
            dataDirectory.getTagDirectory(tag).resolve(oldFile.name).renameTo(newFile)
        }
    }

    fun deleteFile(savedFile: SavedFile) {
        log.info("Deleting file {}.", savedFile.file.name)
        for (tagId in savedFile.tags) {
            removeTag(savedFile.uuid, tagId)
        }
        savedFile.file.delete()
        files.remove(savedFile.uuid)
    }

    // this only adds the tag to the maps, it does not modify the filesystem.
    // to create the symlink, use `addNewTag` below.
    fun addTagInternal(savedFile: SavedFile, tag: Tag) {
        val fileId = savedFile.uuid
        val tagId = tag.uuid
        files[fileId] = savedFile.copy(tags = savedFile.tags.plus(tagId))
        tags[tagId] = tag.copy(files = tag.files.plus(fileId))
    }

    fun addTag(fileId: UUID, tagId: UUID) {
        val savedFile = fileController[fileId] ?: return
        val tag = tagController[tagId] ?: return
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

    fun removeTag(fileId: UUID, tagId: UUID) {
        val savedFile = fileController[fileId] ?: return
        val tag = tagController[tagId] ?: return
        val fileName = savedFile.file.name
        log.info("Removing tag {} from {}.", tag.fullName(), fileName)
        files[fileId] = savedFile.copy(tags = savedFile.tags.minus(tagId))
        tags[tagId] = tag.copy(files = tag.files.minus(fileId))
        dataDirectory.getTagDirectory(tag).resolve(fileName).delete()
    }

    private fun loadFiles() {
        val fileList = dataDirectory.file.listFiles()!!
        for (file in fileList) {
            val savedFile = SavedFile(file = file)
            files[savedFile.uuid] = savedFile
        }
        log.info("Loaded {} files.", fileList.size)
    }

    init {
        loadFiles()
    }

}
