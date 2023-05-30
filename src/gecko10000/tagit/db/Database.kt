package kcash.kcash.misc

import gecko10000.tagit.db.DBUser
import java.io.Closeable
import java.io.File
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*
import java.util.function.Consumer
import kotlin.concurrent.scheduleAtFixedRate

class Database {
    private val sql: SQLHelper = SQLHelper(SQLHelper.openSQLite(File("database.db").toPath()))

    init {
        sql.execute("""
            CREATE TABLE IF NOT EXISTS users (
                username STRING PRIMARY KEY,
                pass_hash STRING NOT NULL
            );
        """.trimIndent())
        sql.execute("""
            CREATE TABLE IF NOT EXISTS user_tokens (
                token STRING NOT NULL,
                creation_time INTEGER NOT NULL,
                user String NOT NULL REFERENCES users(username)
            );
        """.trimIndent())
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

@Suppress("UNCHECKED_CAST")
class SQLHelper(val connection: Connection) : Closeable {

    companion object {

        fun openSQLite(file: Path): Connection {
            val properties = Properties()
            properties.setProperty("foreign_keys", "on")
            properties.setProperty("busy_timeout", "1000")
            return DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath(), properties)
        }
    }

    fun execute(command: String?, vararg fields: Any?) {
        val statement: PreparedStatement = prepareStatement(command, *fields)
        statement.execute()
        statement.close()
    }

    fun executeUpdate(command: String?, vararg fields: Any?): Int {
        val statement: PreparedStatement = prepareStatement(command, *fields)
        val updatedRows = statement.executeUpdate()
        statement.close()
        return updatedRows
    }

    fun <T> querySingleResult(query: String?, vararg fields: Any?): T? {
        val statement: PreparedStatement = prepareStatement(query, *fields)
        val results = statement.executeQuery()
        if (!results.next()) {
            return null
        }
        val obj = results.getObject(1) as T
        results.close()
        statement.close()
        return obj
    }

    fun querySingleResultString(query: String?, vararg fields: Any?): String? {
        val statement: PreparedStatement = prepareStatement(query, *fields)
        val results = statement.executeQuery()
        if (!results.next()) {
            return null
        }
        val string = results.getString(1)
        results.close()
        statement.close()
        return string
    }

    fun querySingleResultBytes(query: String?, vararg fields: Any?): ByteArray? {
        val statement = prepareStatement(query, *fields)
        val results = statement.executeQuery()
        if (!results.next()) {
            return null
        }
        val arr = results.getBytes(1)
        results.close()
        statement.close()
        return arr
    }

    fun querySingleResultLong(query: String?, vararg fields: Any?): Long? {
        val statement: PreparedStatement = prepareStatement(query, *fields)
        val results = statement.executeQuery()
        if (!results.next()) {
            return null
        }
        val long = results.getLong(1)
        results.close()
        statement.close()
        return long
    }

    fun <T> queryResultList(query: String?, vararg fields: Any?): List<T> {
        val list: MutableList<T> = ArrayList()
        val statement: PreparedStatement = prepareStatement(query, *fields)
        val results = statement.executeQuery()
        while (results.next()) {
            list.add(results.getObject(1) as T)
        }
        results.close()
        statement.close()
        return list
    }

    fun queryResultStringList(query: String?, vararg fields: Any?): List<String> {
        val list: MutableList<String> = ArrayList()
        val statement: PreparedStatement = prepareStatement(query, *fields)
        val results = statement.executeQuery()
        while (results.next()) {
            list.add(results.getString(1))
        }
        results.close()
        statement.close()
        return list
    }

    fun queryResultLongList(query: String?, vararg fields: Any?): List<Long> {
        val list: MutableList<Long> = ArrayList()
        val statement: PreparedStatement = prepareStatement(query, *fields)
        val results = statement.executeQuery()
        while (results.next()) {
            list.add(results.getLong(1))
        }
        results.close()
        statement.close()
        return list
    }

    fun queryResults(query: String?, vararg fields: Any?): Results {
        val statement = prepareStatement(query, *fields)
        val results = statement.executeQuery()
        return Results(results, statement)
    }

    fun commit() = connection.commit()

    fun prepareStatement(query: String?, vararg fields: Any?): PreparedStatement {
        val statement = connection.prepareStatement(query)
        var i = 1
        for (obj in fields) {
            statement.setObject(i, obj)
            i++
        }
        return statement
    }

    fun setAutoCommit(autoCommit: Boolean) {
        setCommitInterval(-1)
        connection.autoCommit = autoCommit
    }

    fun isAutoCommit() = connection.autoCommit

    private var timer: Timer? = null

    fun setCommitInterval(ms: Long) {
        timer?.cancel()
        timer = null
        if (ms <= 0) return
        timer = Timer()
        timer!!.scheduleAtFixedRate(ms, ms) { commit() }
    }

    override fun close() {
        setCommitInterval(-1)
        connection.close()
        System.gc()
    }

    class Results constructor(private val results: ResultSet, private val statement: PreparedStatement) :
        AutoCloseable {
        /**
         * @return False if the first call of [ResultSet.next] on the wrapped ResultSet returned false,
         * true otherwise
         */
        var isEmpty = false

        init {
            isEmpty = !results.next()
        }

        /**
         * Moves to the next row in the wrapped ResultSet. Note that this method is called immediately when the
         * Results object is constructed, and does not need to be called to retrieve the items in the first row.
         * @return True if there is another row available in the wrapped ResultSet
         */
        operator fun next(): Boolean {
            return results.next()
        }

        /**
         * Performs an operation on every row in these Results, passing itself each time it iterates to a new row
         * @param lambda The callback to be run on every row in these Results
         */
        fun forEach(lambda: Consumer<Results>) {
            if (isEmpty) {
                return
            }
            lambda.accept(this)
            while (next()) {
                lambda.accept(this)
            }
            close()
        }

        /**
         * Gets an Object in the given column in the current row
         * @param column The index of the column to get, starting at 1
         * @param <T> The type to cast the return value to
         * @return The value in the column
        </T> */
        operator fun <T> get(column: Int): T {
            return results.getObject(column) as T
        }

        fun <T> getNullable(column: Int): T? {
            return results.getObject(column) as T
        }

        /**
         * Gets the bytes in the given column in the current row
         * @param column The index of the column to get, starting at 1
         * @return The bytes in the column
         */
        fun getBytes(column: Int): ByteArray? {
            return results.getBytes(column)
        }

        /**
         * Gets a String in the given column in the current row
         * @param column The index of the column to get, starting at 1
         * @return The String in the column
         * Note: This method exists because [ResultSet.getObject] can return an Integer if the String in the
         * column can be parsed into one.
         */
        fun getString(column: Int): String? {
            return results.getString(column)
        }

        /**
         * Gets a Long in the given column in the current row
         * @param column The index of the column to get, starting at 1
         * @return The String in the column
         * Note: This method exists because [ResultSet.getObject] can return an Integer if the Long in the
         * column can be parsed into one.
         */
        fun getLong(column: Int): Long? {
            return results.getLong(column)
        }

        /**
         * Gets the column count from the returned data
         * @return The column count
         */
        val columnCount: Int
            get() = results.metaData.columnCount

        /**
         * Closes the wrapped ResultSet. Call this when you are done using these Results.
         */
        override fun close() {
            results.close()
            statement.close()
        }
    }

}
