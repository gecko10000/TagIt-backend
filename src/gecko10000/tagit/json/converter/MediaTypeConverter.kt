package gecko10000.tagit.json.converter

import com.google.common.base.Converter
import gecko10000.tagit.json.enum.MediaType

class MediaTypeConverter : Converter<String, MediaType>() {
    override fun doForward(mimeType: String): MediaType {
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

    override fun doBackward(mediaType: MediaType): String {
        throw NotImplementedError()
    }
}
