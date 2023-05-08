package gecko10000.tagit.db

data class DBToken(val user: DBUser, val creationTime: Long) {

    companion object {
        fun fromString(s: String): DBToken? {
            val array = s.toCharArray()
            val builder = StringBuilder()
            for (i in array.indices step 2) {
                val sum = array[i].digitToInt(16) + array[i+1].digitToInt(16) * 16
                builder.append(sum.toChar())
            }
            val decoded = builder.toString()
            val split = decoded.split(' ')
            if (split.size != 3) return null
            val user = DBUser(split[0], split[1])
            val creationTime = split[2].toLongOrNull() ?: return null
            return DBToken(user, creationTime)
        }
    }

    override fun toString(): String {
        val unencoded = "${user.name} ${user.passHash} $creationTime"
        println(unencoded.toByteArray().joinToString("") { it.toString(16) })
        println(unencoded.toByteArray().joinToString("") { String.format("%02x", it) })
        return unencoded.toByteArray().joinToString("") { it.toString(16) }
    }
}
