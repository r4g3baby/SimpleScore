import io.papermc.hangarpublishplugin.model.Platforms
import org.apache.tools.ant.filters.ReplaceTokens
import java.io.ByteArrayOutputStream

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.0"
    id("com.modrinth.minotaur") version "2.8.4"
    kotlin("jvm") version "1.9.20"
}

group = "com.r4g3baby"
version = "3.12.5-dev"

repositories {
    mavenCentral()

    maven(uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/"))
    maven(uri("https://repo.dmulloy2.net/nexus/repository/public/"))
    maven(uri("https://repo.extendedclip.com/content/repositories/placeholderapi/"))
    maven(uri("https://repo.mvdw-software.com/content/groups/public/"))
    maven(uri("https://nexus.neetgames.com/repository/maven-public/"))
    maven(uri("https://repo.codemc.io/repository/maven-public/"))
}

dependencies {
    compileOnly("org.bukkit:bukkit:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.0")
    compileOnly("be.maximvdw:MVdWPlaceholderAPI:3.1.1-SNAPSHOT") {
        exclude("org.spigotmc") // build error
    }
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.1.201") {
        exclude("com.sk89q.worldguard") // build error
    }

    implementation("org.codemc.worldguardwrapper:worldguardwrapper:1.2.1-SNAPSHOT")
    implementation("net.swiftzer.semver:semver:1.3.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.slf4j:slf4j-nop:1.7.36")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    processResources {
        filteringCharset = "UTF-8"
        filesMatching("**plugin.yml") {
            filter<ReplaceTokens>(
                "tokens" to mapOf(
                    "name" to project.name,
                    "version" to project.version,
                    "description" to "A simple animated scoreboard plugin for your server.",
                    "package" to "${project.group}.${project.name.lowercase()}",
                    "website" to "https://r4g3baby.com"
                )
            )
        }
    }

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")

        val libs = "${project.group}.${project.name.lowercase()}.lib"
        relocate("org.codemc.worldguardwrapper", "$libs.wgwrapper")
        relocate("net.swiftzer.semver", "$libs.semver")
        relocate("org.bstats", "$libs.bstats")
        relocate("com.zaxxer.hikari", "$libs.hikari")
        relocate("org.slf4j", "$libs.slf4j")
        relocate("org.jetbrains", "$libs.jetbrains")
        relocate("org.intellij", "$libs.intellij")
        relocate("kotlin", "$libs.kotlin")

        from(file("LICENSE"))

        dependencies {
            exclude("META-INF/**")
        }

        minimize()
    }

    hangarPublish {
        publications.register("plugin") {
            apiKey = findProperty("hangar.token") as String? ?: System.getenv("HANGAR_TOKEN")
            id = findProperty("hangar.project") as String? ?: System.getenv("HANGAR_PROJECT")
            version = project.version as String
            channel = "Release"
            changelog = generateChangelog()

            platforms {
                register(Platforms.PAPER) {
                    jar.set(shadowJar.flatMap { it.archiveFile })
                    platformVersions = mapVersions("hangar.versions")
                }
            }
        }
    }

    modrinth {
        token = findProperty("modrinth.token") as String? ?: System.getenv("MODRINTH_TOKEN")
        projectId = findProperty("modrinth.project") as String? ?: System.getenv("MODRINTH_PROJECT")
        uploadFile = shadowJar.get()
        gameVersions = mapVersions("modrinth.versions")
        loaders = arrayListOf("bukkit", "spigot", "paper")
        changelog = generateChangelog()

        syncBodyFrom = file("README.md").readText()
        modrinth.get().dependsOn(modrinthSyncBody)
    }
}

fun mapVersions(propertyName: String): Provider<List<String>> = provider {
    return@provider (property(propertyName) as String).split(",").map { it.trim() }
}

fun generateChangelog(): Provider<String> = provider {
    val tags = ByteArrayOutputStream().apply {
        exec {
            commandLine("git", "tag", "--sort", "version:refname")
            standardOutput = this@apply
        }
    }.toString(Charsets.UTF_8.name()).trim().split("\n")

    val tagsRange = if (tags.size > 1) {
        "${tags[tags.size - 2]}...${tags[tags.size - 1]}"
    } else if (tags.isNotEmpty()) tags[0] else "HEAD~1...HEAD"

    val repoUrl = findProperty("github.repo") as String? ?: System.getenv("GITHUB_REPO_URL")
    val changelog = ByteArrayOutputStream().apply {
        write("### Commits:\n".toByteArray())

        exec {
            commandLine("git", "log", tagsRange, "--pretty=format:- [%h]($repoUrl/commit/%H) %s", "--reverse")
            standardOutput = this@apply
        }

        write("\n\nCompare Changes: [$tagsRange]($repoUrl/compare/$tagsRange)".toByteArray())
    }.toString(Charsets.UTF_8.name())

    return@provider changelog
}