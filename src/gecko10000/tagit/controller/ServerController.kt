package gecko10000.tagit.controller

import gecko10000.tagit.misc.Config
import gecko10000.tagit.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*

class ServerController(
    val db: DatabaseController,
    val config: Config,
) {
    fun create(): ApplicationEngine {
        return embeddedServer(Netty, port = config.port) {
            install(CORS) {
                allowHost(config.frontendDomain)
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
        }
    }
}
