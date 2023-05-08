package gecko10000.tagit.routing

import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.authRouting() {
    route("/auth") {
        authenticate("auth-bearer") {
            post("register") {

            }
            post("login") {

            }
        }
    }
}
