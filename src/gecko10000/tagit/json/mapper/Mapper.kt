package gecko10000.tagit.json.mapper

class Mapper {
    companion object {
        val CHILD_TAG = ChildTagMapper()
        val MEDIA_TYPE = MediaTypeMapper()
        val SAVED_FILE = SavedFileMapper(MEDIA_TYPE)
        val TAG = TagMapper(CHILD_TAG, SAVED_FILE)
    }
}
