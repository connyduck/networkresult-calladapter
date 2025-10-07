buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.2.20"))
        classpath("org.jlleitschuh.gradle:ktlint-gradle:13.1.0")
        classpath("io.github.gradle-nexus:publish-plugin:2.0.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:2.0.0")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.18.1")
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
