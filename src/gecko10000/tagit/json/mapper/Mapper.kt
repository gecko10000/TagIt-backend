package gecko10000.tagit.json.mapper

class Mapper {
    companion object {
        private val TAG_COUNTS = TagCountsMapper()
        private val MEDIA_TYPE = MediaTypeMapper()
        
        val CHILD_TAG = ChildTagMapper(TAG_COUNTS)
        val SAVED_FILE = SavedFileMapper(MEDIA_TYPE)
        val TAG = TagMapper(CHILD_TAG, SAVED_FILE)
    }
}
