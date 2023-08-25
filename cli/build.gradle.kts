plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover") version "0.7.3"
}

group = "dev.tiebe.avt.x32"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":main"))

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))

    @Suppress("VulnerableLibrariesLocal", "RedundantSuppression") // no other options, safety is not a priority
    implementation("com.illposed.osc:javaosc-core:0.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jfree:jfreechart:1.5.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}