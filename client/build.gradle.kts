plugins {
    kotlin("js") version "1.6.0"
}

group = "ru.altmanea.eduKtor"
version = "0.1"

repositories {
    mavenCentral()
    flatDir {
        dirs("$projectDir/../model/build/libs")
    }
}

val kotlinWrappersVersion = "0.0.1-pre.279-kotlin-1.6.0"
fun kotlinw(target: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target"

dependencies {
    implementation("ru.altmanea.edu-ktor:model-js-0.1")
    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:${kotlinWrappersVersion}"))
    implementation(kotlinw("react"))
    implementation(kotlinw("react-dom"))
    implementation(kotlinw("react-router-dom"))
    implementation(kotlinw("redux"))
    implementation(kotlinw("react-redux"))
    implementation(kotlinw("react-query"))
    implementation(kotlinw("styled"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.1")
    implementation(npm("axios", "0.24.0"))
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
}