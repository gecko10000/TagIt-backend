package gecko10000.tagit.routing

import io.ktor.server.routing.*
import java.util.*

data class UserToken(val username: String, val passHash: String, val creationTime: Long) {

    companion object {
        fun fromString(s: String): UserToken? {
            val split = s.split('|')
            if (split.size != 3) return null
            val decoder = Base64.getDecoder()
            try {
                val username = decoder.decode(split[0]).toString()
                val passHash = decoder.decode(split[1]).toString()
                val creationTime = decoder.decode(split[2]).toString().toLongOrNull() ?: return null
                return UserToken(username, passHash, creationTime)
            } catch (ex: IllegalArgumentException) {
                return null
            }
        }
    }
    override fun toString(): String {
        val encoder = Base64.getEncoder()
        val eUser = encoder.encodeToString(username.toByteArray())
        val ePass = encoder.encodeToString(passHash.toByteArray())
        val eTime = encoder.encodeToString(creationTime.toString().toByteArray())
        return "$eUser|$ePass|$eTime"
    }

}

fun Route.authRouting() {
    route("/auth") {
        post("register") {

        }
        post("login") {

        }
    }
}
