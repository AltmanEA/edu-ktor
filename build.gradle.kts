val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val arrow_version = "1.0.1"

plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "ru.altmanea.edu-ktor"
version = "0.0.1"

application {
    mainClass.set("ru.altmanea.edu.ktor.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
    flatDir {
        dirs("$projectDir/model/build/libs")
    }
}

dependencies {
    implementation("ru.altmanea.edu-ktor:model-jvm-0.1")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}