package gecko10000.tagit.controller

import gecko10000.tagit.dataDirectory
import gecko10000.tagit.db.DBUser
import gecko10000.tagit.db.SQLHelper

class DatabaseController {
    private val sql: SQLHelper = SQLHelper(SQLHelper.openSQLite(dataDirectory.base.resolve("database.db").toPath()))

    init {
        createTables()
    }

    private fun createTables() {
        sql.execute(
            """
            CREATE TABLE IF NOT EXISTS users (
                username STRING PRIMARY KEY,
                pass_hash STRING NOT NULL
            );
        """.trimIndent()
        )
        sql.execute(
            """
            CREATE TABLE IF NOT EXISTS user_tokens (
                token STRING NOT NULL,
                creation_time INTEGER NOT NULL,
                user String NOT NULL REFERENCES users(username)
            );
        """.trimIndent()
        )
    }

    fun addUser(user: DBUser) {
        sql.execute("INSERT OR IGNORE INTO users VALUES (?, ?);", user.name, user.passHash)
    }

    fun getUser(username: String): DBUser? {
        val results = sql.queryResults("SELECT * FROM users WHERE username=?", username)
        if (results.isEmpty) return null
        return DBUser(results.getString(1)!!, results.getString(2)!!)
    }

    fun countUsers(): Int = sql.querySingleResult<Int>("SELECT COUNT(*) FROM users;")!!

    fun insertToken(token: String, user: DBUser) {
        // having a creation time will let us expire tokens (should we?)
        sql.execute("INSERT OR IGNORE INTO user_tokens VALUES (?, ?, ?);", token, System.currentTimeMillis(), user.name)
    }

    fun userFromToken(token: String): DBUser? {
        val username = sql.querySingleResultString("SELECT user FROM user_tokens WHERE token=?", token)
        if (username.isNullOrEmpty()) return null
        return getUser(username)
    }

}
