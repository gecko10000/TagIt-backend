package gecko10000.tagit.db

data class DBToken(val user: DBUser, val creationTime: Long) {

    companion object {
        fun fromString(s: String): DBToken? {
            try {
                val array = s.toCharArray()
                val builder = StringBuilder()
                // no out of bounds for array[i + 1]
                if (array.size % 2 != 0) return null

                for (i in array.indices step 2) {
                    // decode both parts of the byte
                    val sum = array[i].digitToInt(16) + array[i + 1].digitToInt(16) * 16
                    builder.append(sum.toChar())
                }
                val decoded = builder.toString()
                // string-separated parameters
                val split = decoded.split(' ')
                if (split.size != 3) return null
                val user = DBUser(split[0], split[1])
                val creationTime = split[2].toLongOrNull() ?: return null
                return DBToken(user, creationTime)
            } catch (ex: Exception) {
                ex.printStackTrace()
                return null
            }
        }
    }

    override fun toString(): String {
        val unencoded = "${user.name} ${user.passHash} $creationTime"
        return unencoded.toByteArray().joinToString("") { String.format("%02x", it) }
    }
}
