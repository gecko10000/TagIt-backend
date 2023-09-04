package gecko10000.tagit.json.mapper

import gecko10000.tagit.dimensionsController

class JsonMapper {
    companion object {
        private val TAG_COUNTS = TagCountsMapper()
        private val DIMENSIONS = DimensionsMapper()

        val CHILD_TAG = ChildTagMapper(TAG_COUNTS)
        val SAVED_FILE = SavedFileMapper(DIMENSIONS, dimensionsController)
        val TAG = TagMapper(CHILD_TAG, SAVED_FILE)
    }
}
