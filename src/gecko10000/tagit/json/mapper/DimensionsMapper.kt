package gecko10000.tagit.json.mapper

import gecko10000.tagit.json.`object`.JsonDimensions
import gecko10000.tagit.model.Dimensions

class DimensionsMapper : (Dimensions) -> JsonDimensions {
    override fun invoke(dimensions: Dimensions): JsonDimensions {
        return JsonDimensions(
            dimensions.width,
            dimensions.height,
        )
    }
}
