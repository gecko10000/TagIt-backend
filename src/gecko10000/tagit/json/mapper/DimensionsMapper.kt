package gecko10000.tagit.json.mapper

import gecko10000.tagit.json.`object`.JsonDimensions
import gecko10000.tagit.model.Dimensions
import java.util.function.Function

class DimensionsMapper : Function<Dimensions, JsonDimensions> {
    override fun apply(dimensions: Dimensions): JsonDimensions {
        return JsonDimensions(
            dimensions.width,
            dimensions.height,
        )
    }
}
