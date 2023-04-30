package gecko10000.tagit.objects

import gecko10000.tagit.serializers.FileSerializer
import gecko10000.tagit.serializers.TagStringSerializer
import io.ktor.server.http.content.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

@Serializable
data class SavedFile(
    @SerialName("name")
    @Serializable(with = FileSerializer::class)
    val file: File,
    val mimeType: String,
    val tags: MutableSet<@Serializable(with = TagStringSerializer::class) Tag> = ConcurrentSkipListSet(compareBy { it.name })
) : Nameable() {
    constructor(file: File) : this(
        // withoutParameters() removes, for example, `; charset=UTF-8` from `text/plain; charset=UTF-8`
        file, LocalFileContent(file).contentType.withoutParameters().toString()
    )

    override fun name(): String = file.name
}
