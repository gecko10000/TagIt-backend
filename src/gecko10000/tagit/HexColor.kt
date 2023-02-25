package gecko10000.tagit

data class HexColor(val r: Int, val g: Int, val b: Int) {
    override fun toString(): String {
        return String.format("#%02d%02d%02d", r, g, b)
    }
}
