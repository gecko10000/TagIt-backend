package gecko10000.tagit.misc

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import de.mkammerer.argon2.Argon2Helper

private const val memory = 65536
private const val parallelism = 1

private val argon: Argon2 = Argon2Factory.create()
private val iterations = Argon2Helper.findIterations(argon, 1000, memory, parallelism)

fun hash(message: CharArray): String {
    val hash: String = argon.hash(iterations, memory, parallelism, message)
    argon.wipeArray(message)
    return hash
}

fun verify(message: CharArray, hash: String): Boolean {
    val verified = argon.verify(hash, message)
    argon.wipeArray(message)
    return verified
}
