package gecko10000.tagit.routing

import gecko10000.tagit.db
import gecko10000.tagit.misc.VERSION
import gecko10000.tagit.misc.extensions.respondJson
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonPrimitive

fun Routing.idRouting() {
    route("/tagit") {
        get("version") {
            call.respondJson(mapOf("version" to JsonPrimitive(VERSION), "users" to JsonPrimitive(db.countUsers())))
        }
    }
}
