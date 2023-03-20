package gecko10000.tagit

import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import java.io.File
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
            savedFiles[file.name] = savedFile.addTags(parent!!)
            return
        }
        // file is a directory, call recursively
        val tag = Tag(file.name, parent?.name)
        parent?.run { tags[fullName()] = addSubTags(tag) }
        tags[tag.fullName()] = tag
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

    init {
        loadFiles()
        loadTags()
    }
}
