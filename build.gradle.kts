buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.jetbrains.kotlinx.kover") version "0.7.3"
}

dependencies {
    kover(project(":main"))
    kover(project(":cli"))
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
