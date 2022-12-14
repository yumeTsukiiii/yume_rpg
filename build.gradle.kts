import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    `maven-publish`
    `java-library`
}

group = "fan.yumetsuki"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

fun getJ2v8SupportSystem(): String? {
    val system = DefaultNativePlatform.getCurrentOperatingSystem()
    val architecture = DefaultNativePlatform.getCurrentArchitecture()
    if (system.isWindows) {
        if (architecture.isI386) {
            return "win32_x86"
        }
        if (architecture.isAmd64) {
            return "win32_x86_64"
        }
    }
    if (system.isMacOsX) {
        return "macosx_x86_64"
    }
    return null
}

dependencies {
    getJ2v8SupportSystem()?.let { j2v8System ->
        implementation("com.eclipsesource.j2v8:j2v8_${j2v8System}:4.6.0")
    } ?: error("仅支持 windows / macOS")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("YumeRpg") {
            groupId = "fan.yumetsuki"
            artifactId = "yume-rpg"

            from(components["java"])
        }
    }

    repositories {
        mavenLocal()
    }
}