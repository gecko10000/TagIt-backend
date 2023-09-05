import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// useful tasks:
// run - run the program directly
// shadowJar - create an executable jarfile

// usable in the project as `VERSION` - if not, run the generateVersion task.
version = "0.1"

plugins {
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

// generates a file with the current version in it
// https://stackoverflow.com/a/74771876
val generateVersion by tasks.registering(Sync::class) {
    val pkg = listOf("gecko10000", "tagit", "misc")
    val resource = project.resources.text.fromString(
        """
        |package ${pkg.joinToString(".")}
        |
        |const val VERSION = "$version"
        |
    """.trimMargin()
    )
    from(resource) {
        rename { "Version.kt" }
        into(pkg.joinToString("/"))
    }
    into(layout.buildDirectory.dir("generated/src/"))
}

sourceSets {
    main {
        java {
            srcDir("src")
            srcDir(generateVersion.map { it.destinationDir })
        }
        resources {
            srcDir("res")
        }
    }
}

group = "gecko10000.tagit"

repositories {
    mavenCentral()
    maven("https://redempt.dev")

}

val ktorVersion = "2.2.4"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-partial-content:$ktorVersion")

    implementation("com.github.Redempt:RedLex:1.3.5")
    implementation("org.xerial:sqlite-jdbc:3.41.2.1")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("de.mkammerer:argon2-jvm:2.11")
    implementation("net.bramp.ffmpeg:ffmpeg:0.7.0")
    implementation("org.yaml:snakeyaml:2.2")
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<Wrapper> {
    dependsOn(generateVersion)
}

tasks.withType<KotlinCompile> {
    dependsOn(generateVersion)
}


application {
    mainClass.set("gecko10000.tagit.TagItKt")
}
