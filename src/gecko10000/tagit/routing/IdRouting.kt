package gecko10000.tagit.routing

import gecko10000.tagit.json.`object`.JsonBackendInfo
import gecko10000.tagit.misc.extension.respondJson
import io.ktor.server.application.*
import io.ktor.server.routing.*

private fun Route.versionRoute() {
    get("version") {
        val backendInfo = JsonBackendInfo.generate()
        call.respondJson(backendInfo)
    }
}

fun Routing.idRouting() {
    route("/tagit") {
        versionRoute()
    }
}
