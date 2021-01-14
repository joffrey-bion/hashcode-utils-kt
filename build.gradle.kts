import com.jfrog.bintray.gradle.BintrayExtension.GpgConfig
import com.jfrog.bintray.gradle.BintrayExtension.MavenCentralSyncConfig
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.20"
    id("com.jfrog.bintray") version "1.8.4"
    id("org.hildan.github.changelog") version "0.8.0"
}

group = "org.hildan.hashcode"
description = "Utilities for programs solving Google HashCode problems"

val Project.labels: Array<String>
    get() = arrayOf("google", "hashcode", "utils", "kotlin", "hashcode-utils", "hashcode-utils-kt")

val Project.licenses: Array<String>
    get() = arrayOf("MIT")

changelog {
    futureVersionTag = project.version.toString()
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.slf4j:slf4j-api:1.7.24")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.0")
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

val githubUser = getPropOrEnv("githubUser", "GITHUB_USER")
val githubRepoName = rootProject.name
val githubSlug = "$githubUser/${rootProject.name}"
val githubRepoUrl = "https://github.com/$githubSlug"

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val dokkaJavadocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka into a Javadoc jar"
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            artifact(sourcesJar)
            artifact(dokkaJavadocJar)

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set(githubRepoUrl)
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("joffrey-bion")
                        name.set("Joffrey Bion")
                        email.set("joffrey.bion@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:$githubRepoUrl.git")
                    developerConnection.set("scm:git:git@github.com:$githubSlug.git")
                    url.set(githubRepoUrl)
                }
            }
        }
    }
}

bintray {
    user = getPropOrEnv("bintrayUser", "BINTRAY_USER")
    key = getPropOrEnv("bintrayApiKey", "BINTRAY_KEY")
    setPublications("maven")
    publish = true

    pkg(closureOf<PackageConfig> {
        repo = getPropOrEnv("bintrayRepo", "BINTRAY_REPO")
        name = project.name
        desc = project.description
        setLabels(*project.labels)
        setLicenses(*project.licenses)

        websiteUrl = githubRepoUrl
        issueTrackerUrl = "$githubRepoUrl/issues"
        vcsUrl = "$githubRepoUrl.git"
        githubRepo = githubSlug

        version(closureOf<VersionConfig> {
            desc = project.description
            vcsTag = project.version.toString()
            gpg(closureOf<GpgConfig> {
                sign = true
            })
            mavenCentralSync(closureOf<MavenCentralSyncConfig> {
                sync = true
                user = getPropOrEnv("ossrhUserToken", "OSSRH_USER_TOKEN")
                password = getPropOrEnv("ossrhKey", "OSSRH_KEY")
            })
        })
    })
}
tasks.bintrayUpload.get().dependsOn(tasks.build)

fun Project.getPropOrEnv(propName: String, envVar: String? = null): String? =
    findProperty(propName) as String? ?: System.getenv(envVar)
