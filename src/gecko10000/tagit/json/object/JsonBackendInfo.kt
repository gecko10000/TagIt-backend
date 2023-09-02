package gecko10000.tagit.json.`object`

import gecko10000.tagit.db
import gecko10000.tagit.misc.VERSION
import kotlinx.serialization.Serializable

@Serializable
data class JsonBackendInfo(
    val version: String,
    val users: Int,
) {
    companion object {
        fun generate(): JsonBackendInfo {
            return JsonBackendInfo(VERSION, db.countUsers())
        }
    }
}
