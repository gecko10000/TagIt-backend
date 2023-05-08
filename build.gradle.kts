import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// useful tasks:
// run - run the program directly
// installDist - creates a bash script in build/install/
// distZip - creates a zip in build/distributions/

plugins {
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
    application
}

sourceSets {
    main {
        java {
            srcDir("src")
        }
        resources {
            srcDir("res")
        }
    }
}

group = "gecko10000.tagit"
version = "0.1"

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

    implementation("com.github.Redempt:RedLex:1.3.5")
    implementation("org.xerial:sqlite-jdbc:3.41.2.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("gecko10000.tagit.TagItKt")
}
