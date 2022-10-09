import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "fan.yumetsuki"
version = "1.0-SNAPSHOT"

repositories {
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
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}