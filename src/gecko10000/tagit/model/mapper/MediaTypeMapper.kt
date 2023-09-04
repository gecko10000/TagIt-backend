package gecko10000.tagit.model.mapper

import gecko10000.tagit.model.enum.MediaType
import java.util.function.Function

class MediaTypeMapper : Function<String, MediaType> {

    override fun apply(mimeType: String): MediaType {
        if (mimeType.startsWith("image")) {
            return MediaType.IMAGE
        } else if (mimeType.startsWith("video")) {
            return MediaType.VIDEO
        } else if (mimeType.startsWith("audio")) {
            return MediaType.AUDIO
        } else if (mimeType.startsWith("text")) {
            return MediaType.TEXT
        }
        return MediaType.UNKNOWN
    }
}
