import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// useful tasks:
// run - run the program directly
// installDist - creates a bash script in build/install/
// distZip - creates a zip in build/distributions/

plugins {
    kotlin("jvm") version "1.8.10"
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
}

val ktorVersion = "2.2.3"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("gecko10000.tagit.TagItKt")
}
