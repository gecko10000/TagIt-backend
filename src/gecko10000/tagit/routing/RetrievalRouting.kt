package gecko10000.tagit.routing

import gecko10000.tagit.fileController
import gecko10000.tagit.json.mapper.JsonMapper
import gecko10000.tagit.misc.extension.respondJson
import gecko10000.tagit.model.enum.FileOrder
import gecko10000.tagit.model.enum.TagOrder
import gecko10000.tagit.model.enum.filesReversed
import gecko10000.tagit.model.enum.tagsReversed
import gecko10000.tagit.tagController
import io.ktor.server.application.*
import io.ktor.server.routing.*

private fun Route.allTagsRoute() {
    get("all") {
        val headers = call.request.headers
        val tagOrder = TagOrder.get(headers)
        val reversed = headers.tagsReversed()
        var sortedTags = tagController.readOnlyTagMap().values.sortedWith(tagOrder.comparator)
        if (reversed) sortedTags = sortedTags.reversed()
        val json = sortedTags.map { JsonMapper.CHILD_TAG(it) }
        call.respondJson(mapOf("tags" to json))
    }
}

private fun Route.allFilesRoute() {
    get("all") {
        val headers = call.request.headers
        val fileOrder = FileOrder.get(headers)
        val filesReversed = headers.filesReversed()
        var sortedFiles = fileController.readOnlyFileMap().values.sortedWith(fileOrder.comparator)
        if (filesReversed) sortedFiles = sortedFiles.reversed()
        // TODO: defaults instead of retrieving tag ordering from headers?
        val json = sortedFiles.map { JsonMapper.SAVED_FILE(it, TagOrder.get(headers), headers.tagsReversed()) }
        call.respondJson(mapOf("files" to json))
    }
}

fun Route.retrievalRouting() {
    route("/tags") {
        allTagsRoute()
    }
    route("/files") {
        allFilesRoute()
    }
}
