package gecko10000.tagit.misc

import gecko10000.tagit.controller.DimensionsController
import gecko10000.tagit.model.SavedFile
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class SavedFileMap(private val dimensionsController: DimensionsController) : ConcurrentHashMap<String, SavedFile>() {
    private val log = LoggerFactory.getLogger(this::class.java)

    // note: `operator fun set` simply calls `put` in MutableMap (superclass of CHM)
    override fun put(key: String, value: SavedFile): SavedFile? {
        dimensionsController.determineSize(value)
        return super.put(key, value)
    }

    override fun remove(key: String): SavedFile? {
        val savedFile = super.remove(key)
        savedFile?.let { dimensionsController.removeSavedFile(it) }
        return savedFile
    }

    override fun remove(key: String, value: SavedFile): Boolean {
        dimensionsController.removeSavedFile(value)
        return super.remove(key, value)
    }
}
