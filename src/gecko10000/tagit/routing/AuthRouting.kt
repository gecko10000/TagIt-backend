package gecko10000.tagit.routing

import gecko10000.tagit.db
import gecko10000.tagit.db.DBUser
import gecko10000.tagit.misc.hash
import gecko10000.tagit.misc.respondJson
import gecko10000.tagit.misc.verify
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.util.*

// TODO: improve token generation
private fun newToken(user: DBUser) = UUID.randomUUID().toString()

private suspend fun PipelineContext<Unit, ApplicationCall>.register() {
    // this endpoint can be called unauthenticated,
    // but only when there are no users in the database
    val principal = call.principal<UserIdPrincipal>()
    if (principal == null && db.countUsers() != 0) {
        return call.respond(BadRequest, "You must be logged in to create extra users.")
    }
    val params = call.receiveParameters()
    val username = params["username"] ?: return call.respond(BadRequest, "No username provided.")
    val password = params["password"] ?: return call.respond(BadRequest, "No password provided.")
    // if user exists, return early
    db.getUser(username)?.run { return call.respond(BadRequest, "Username is taken.") }
    val passHash = hash(password.toCharArray())
    val user = DBUser(username, passHash)
    db.addUser(user)
    call.respond(OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.login() {
    val params = call.receiveParameters()
    val username = params["username"] ?: return call.respond(BadRequest, "No username provided.")
    val password = params["password"] ?: return call.respond(BadRequest, "No password provided.")
    // if user doesn't exist, return
    val user = db.getUser(username) ?: return call.respond(BadRequest, "User does not exist.")
    // wrong password
    if (!verify(password.toCharArray(), user.passHash)) {
        return call.respond(BadRequest, "Incorrect password.")
    }
    val token = newToken(user)
    db.insertToken(token, user)
    return call.respondJson(mapOf("token" to token))
}

fun Route.authRouting() {
    route("/auth") {
        // only existing users can create new accounts
        authenticate("auth-bearer", optional = true) {
            post("register") {
                register()
            }
        }
        // only non-authenticated endpoint
        post("login") {
            login()
        }
    }
}
