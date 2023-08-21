pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":cli")
include(":main")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}