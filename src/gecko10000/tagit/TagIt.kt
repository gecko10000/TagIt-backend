package gecko10000.tagit

import gecko10000.tagit.misc.Config
import gecko10000.tagit.model.SavedFile
import gecko10000.tagit.model.Tag
import gecko10000.tagit.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kcash.kcash.misc.Database
import java.util.concurrent.ConcurrentHashMap

val savedFiles = ConcurrentHashMap<String, SavedFile>()
val tags = ConcurrentHashMap<String, Tag>()
val fileManager = FileManager()
val db = Database()

fun main() {
    embeddedServer(Netty, port = Config.PORT) {
        install(CORS) {
            anyHost()
            allowHeaders { true }
            allowMethod(HttpMethod.Patch)
            allowMethod(HttpMethod.Delete)
        }
        install(Authentication) {
            bearer("auth-bearer") {
                authenticate { cred ->
                    val user = db.userFromToken(cred.token)
                    user?.name?.let { UserIdPrincipal(it) }
                }
            }
        }
        install(CallLogging)
        routing {
            authenticate("auth-bearer") {
                fileRouting()
                tagRouting()
                retrievalRouting()
                searchRouting()
            }
            authRouting()
            idRouting()
        }
    }.start(wait = true)
}
