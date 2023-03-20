package gecko10000.tagit

import gecko10000.tagit.objects.SavedFile
import gecko10000.tagit.objects.Tag
import gecko10000.tagit.routing.fileRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

val savedFiles = ConcurrentHashMap<String, SavedFile>()
val tags = ConcurrentHashMap<String, Tag>()
val fileManager = FileManager()

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 10000) {
        routing {
            fileRouting()
        }
    }.start(wait = true)
}
