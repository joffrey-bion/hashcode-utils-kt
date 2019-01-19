plugins {
    kotlin("jvm") version "1.3.11"
}

group = "org.hildan.hashcode"
version = "1.0.0"
description = "Utilities for programs solving Google HashCode problems"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    implementation("org.slf4j:slf4j-api:1.7.24")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}
