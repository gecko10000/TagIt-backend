package gecko10000.tagit

import gecko10000.tagit.db.DBToken
import gecko10000.tagit.db.DBUser
import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
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
    embeddedServer(Netty, port = 10000) {
        install(CORS) {
            anyHost()
            allowHeaders { true }
            allowMethod(HttpMethod.Patch)
            allowMethod(HttpMethod.Delete)
        }
        install(Authentication) {
            bearer("auth-bearer") {
                authenticate { tokenCredential ->
                    val token = DBToken(DBUser("hello", "world"), 1234)
                    //val token = DBToken.fromString(tokenCredential.token) ?: return@authenticate null
                    println(token)
                    UserIdPrincipal(token.user.name)
                }
            }
        }
        install(CallLogging)
        routing {
            fileRouting()
            tagRouting()
            retrievalRouting()
            searchRouting()
            authRouting()
        }
    }.start(wait = true)
}
