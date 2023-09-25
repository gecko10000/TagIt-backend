package gecko10000.tagit.routing

import gecko10000.tagit.db
import gecko10000.tagit.db.DBUser
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.misc.hash
import gecko10000.tagit.misc.verify
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

// TODO: improve token generation
private fun newToken(user: DBUser) = UUID.randomUUID().toString()

private fun Route.registerRoute() {
    post("register") {
        // this endpoint can be called unauthenticated,
        // but only when there are no users in the database
        val principal = call.principal<UserIdPrincipal>()
        if (principal == null && db.countUsers() != 0) {
            return@post call.respond(BadRequest, "You must be logged in to create extra users.")
        }
        val params = call.receiveParameters()
        val username = params["username"] ?: return@post call.respond(BadRequest, "No username provided.")
        val password = params["password"] ?: return@post call.respond(BadRequest, "No password provided.")
        // if user exists, return@post early
        db.getUser(username)?.run { return@post call.respond(BadRequest, "Username is taken.") }
        val passHash = hash(password.toCharArray())
        val user = DBUser(username, passHash)
        db.addUser(user)
        call.respond(OK)
    }
}

private fun Route.loginRoute() {
    post("login") {
        val params = call.receiveParameters()
        val username = params["username"] ?: return@post call.respond(BadRequest, "No username provided.")
        val password = params["password"] ?: return@post call.respond(BadRequest, "No password provided.")
        // if user doesn't exist, return@post
        val user = db.getUser(username) ?: return@post call.respond(BadRequest, "User does not exist.")
        // wrong password
        if (!verify(password.toCharArray(), user.passHash)) {
            return@post call.respond(BadRequest, "Incorrect password.")
        }
        val token = newToken(user)
        db.insertToken(token, user)
        return@post call.respondJson(mapOf("token" to token))
    }
}

fun Route.authRouting() {
    route("/auth") {
        // only existing users can create new accounts
        // unless there are no users in the system
        authenticate("auth-bearer", optional = true) {
            registerRoute()
        }
        loginRoute()
    }
}
