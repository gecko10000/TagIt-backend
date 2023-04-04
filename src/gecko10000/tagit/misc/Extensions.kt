package gecko10000.tagit.misc

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend inline fun <reified T> ApplicationCall.respondJson(toEncode: T) {
    this.respondText(Json.encodeToString(toEncode), contentType = ContentType.Application.Json)
}
