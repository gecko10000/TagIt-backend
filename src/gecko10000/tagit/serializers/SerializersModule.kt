package gecko10000.tagit.serializers

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val serializers = SerializersModule {
    contextual(FileSerializer())
    contextual(SavedFileStringSerializer())
    contextual(TagStringSerializer())
    contextual(TagNameSerializer())
}
