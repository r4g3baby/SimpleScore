plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.10"
    id("maven-publish")
}

group = "com.r4g3baby"
version = "3.12.3-dev"

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

    implementation("org.codemc.worldguardwrapper:worldguardwrapper:1.2.0-SNAPSHOT")
    implementation("net.swiftzer.semver:semver:1.2.0")
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
            filter<org.apache.tools.ant.filters.ReplaceTokens>(
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
            exclude("META-INF/NOTICE")
            exclude("META-INF/maven/**")
            exclude("META-INF/versions/**")
            exclude("META-INF/**.kotlin_module")
        }

        minimize()
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/r4g3baby/SimpleScore")
            credentials {
                username = project.findProperty("github.actor") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            artifactId = project.name.lowercase()
            from(components["kotlin"])
        }
    }
}