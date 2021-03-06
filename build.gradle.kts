buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.6.21"))
        classpath("org.jlleitschuh.gradle:ktlint-gradle:10.3.0")
        classpath("io.github.gradle-nexus:publish-plugin:1.1.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.6.21")
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
    delete(rootProject.buildDir)
}

apply(from = "$rootDir/scripts/publish-root.gradle")
