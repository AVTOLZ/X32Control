plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover") version "0.7.3"
}

group = "dev.tiebe.avt.x32"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
    implementation("com.illposed.osc:javaosc-core:0.8")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")

    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("org.apache.commons:commons-math3:3.6.1")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}