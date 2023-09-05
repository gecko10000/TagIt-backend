package gecko10000.tagit.controller

import gecko10000.tagit.config
import gecko10000.tagit.db
import gecko10000.tagit.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.routing.*

class ServerController {
    fun create(): ApplicationEngine {
        return embeddedServer(Netty, port = config.port) {
            install(CORS) {
                if (config.frontendDomain == "*") {
                    anyHost()
                } else {
                    allowHost(config.frontendDomain)
                }
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
            install(PartialContent)
            routing {
                authenticate("auth-bearer") {
                    retrievalRouting()
                    searchRouting()
                    tagRouting()
                }
                // not authenticated for login (and registration)
                authRouting()
                // not authenticated for query param authentication
                // for file retrieval
                fileRouting()
                // not authenticated for publicly available info
                idRouting()
            }
        }
    }
}
