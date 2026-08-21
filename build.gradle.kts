import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    id("maven-publish")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.dokka)
    alias(libs.plugins.shadow)
    alias(libs.plugins.hangar)
    alias(libs.plugins.minotaur)
}

group = "com.r4g3baby.simplescore"
version = "4.2.1-dev"

dependencies {
    api(project("bukkit"))

    dokka(project(":api"))
    dokka(project(":core"))
    dokka(project(":bukkit"))
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = "${rootProject.name}-${path.replace(":", "-").drop(1)}".lowercase()

                from(components["java"])
            }
        }
    }
}

allprojects {
    java {
        withSourcesJar()
    }

    kotlin {
        jvmToolchain(8)
    }
}

tasks {
    jar { enabled = false }

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")

        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }

        val libs = "${project.group}.lib"
        relocate("org.objenesis", "$libs.objenesis")
        relocate("net.swiftzer.semver", "$libs.semver")
        relocate("org.bstats", "$libs.bstats")

        relocate("org.jetbrains", "$libs.jetbrains")
        relocate("kotlin", "$libs.kotlin")

        from(file("LICENSE"))
        exclude("META-INF/**")

        minimize()
    }

    hangarPublish {
        publications.register("plugin") {
            apiKey = findProperty("hangar.token") as String? ?: System.getenv("HANGAR_TOKEN")
            id = property("hangar.project") as String?
            version = project.version as String
            channel = "Release"
            changelog = parseGitHubChangelog()

            platforms {
                register(Platforms.PAPER) {
                    jar.set(shadowJar.flatMap { it.archiveFile })
                    platformVersions = mapVersions("hangar.versions")
                }
            }

            pages {
                resourcePage(readme())
            }
        }
    }

    publishAllPublicationsToHangar.get().dependsOn(syncAllPagesToHangar)

    modrinth {
        token = findProperty("modrinth.token") as String? ?: System.getenv("MODRINTH_TOKEN")
        projectId = property("modrinth.project") as String?
        uploadFile = shadowJar.get()
        gameVersions = mapVersions("modrinth.versions")
        loaders = arrayListOf("bukkit", "spigot", "paper", "folia", "purpur")
        changelog = parseGitHubChangelog()

        syncBodyFrom = readme()
    }

    modrinth.get().dependsOn(modrinthSyncBody)
}

fun readme(): Provider<String> = provider {
    return@provider file("README.md").readText()
}

fun mapVersions(propertyName: String): Provider<List<String>> = provider {
    return@provider (property(propertyName) as String).split(",").map { it.trim() }
}

fun parseGitHubChangelog(): Provider<String> = provider {
    val changelog = System.getenv("GITHUB_CHANGELOG") ?: return@provider "(No changelog provided)"

    val userRegex = Regex("(?<!\\w)@([A-Za-z0-9-]+)")
    val pullRegex = Regex("https://github\\.com/[\\w-]+/[\\w-]+/pull/(\\d+)")
    val compareRegex = Regex("https://github\\.com/[\\w-]+/[\\w-]+/compare/([\\w\\.]+)")

    return@provider changelog.replace(userRegex) {
        val user = it.groupValues[1]
        "[@$user](https://github.com/$user)"
    }.replace(pullRegex) {
        val pr = it.groupValues[1]
        "[#$pr](${it.value})"
    }.replace(compareRegex) {
        val range = it.groupValues[1]
        "[$range](${it.value})"
    }
}
