package gecko10000.tagit.routing

import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.idRouting() {
    route("/is/this/a/tagit/backend") {
        get {
            call.respond(OK, "It sure is.")
        }
    }
}
