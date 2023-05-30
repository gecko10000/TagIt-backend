package gecko10000.tagit.routing

import gecko10000.tagit.db
import gecko10000.tagit.misc.VERSION
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.idRouting() {
    route("/tagit") {
        get("version") {
            call.respond(mapOf("version" to VERSION, "users" to db.countUsers()))
        }
    }
}
