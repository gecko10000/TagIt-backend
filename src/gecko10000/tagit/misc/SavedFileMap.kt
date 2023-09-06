package gecko10000.tagit.misc

import gecko10000.tagit.model.SavedFile
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

private typealias ListenerFunction = (SavedFile) -> Unit

class SavedFileMap : ConcurrentHashMap<String, SavedFile>() {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val putListeners: MutableList<ListenerFunction> = ArrayList()
    private val removeListeners: MutableList<ListenerFunction> = ArrayList()

    fun addPutListener(listener: ListenerFunction) = putListeners.add(listener)
    fun addRemoveListener(listener: ListenerFunction) = removeListeners.add(listener)

    // note: `operator fun set` simply calls `put` in MutableMap (superclass of CHM)
    override fun put(key: String, value: SavedFile): SavedFile? {
        putListeners.map { it(value) }
        return super.put(key, value)
    }

    override fun remove(key: String): SavedFile? {
        val savedFile = super.remove(key)
        savedFile?.let { removeListeners.map { it(savedFile) } }
        return savedFile
    }

    override fun remove(key: String, value: SavedFile): Boolean {
        val removed = super.remove(key, value)
        if (removed) {
            removeListeners.map { it(value) }
        }
        return removed
    }
}
