buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.9.24"))
        classpath("org.jlleitschuh.gradle:ktlint-gradle:12.1.0")
        classpath("io.github.gradle-nexus:publish-plugin:1.3.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
    }
}

apply(plugin = "io.github.gradle-nexus.publish-plugin")

allprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.layout.buildDirectory)
}

apply(from = "$rootDir/scripts/publish-root.gradle")
