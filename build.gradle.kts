plugins {
    id("com.github.johnrengelman.shadow") version "7.1.1"
    kotlin("jvm") version "1.6.10"
}

group = "com.r4g3baby"
version = "3.8.1"

repositories {
    mavenCentral()

    maven(uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots"))
    maven(uri("https://repo.dmulloy2.net/nexus/repository/public/"))
    maven(uri("https://repo.extendedclip.com/content/repositories/placeholderapi/"))
    maven(uri("https://repo.mvdw-software.com/content/groups/public/"))
    maven(uri("https://nexus.neetgames.com/repository/maven-public/"))
    maven(uri("https://repo.codemc.io/repository/maven-public/"))
}

dependencies {
    compileOnly("org.bukkit:bukkit:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.8.2")
    compileOnly("be.maximvdw:MVdWPlaceholderAPI:3.1.1-SNAPSHOT") {
        exclude("org.spigotmc") // build error
    }
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.1.201") {
        exclude("com.sk89q.worldguard") // build error
    }

    implementation("org.codemc.worldguardwrapper:worldguardwrapper:1.2.0-SNAPSHOT")
    implementation("net.swiftzer.semver:semver:1.1.2")
    implementation("org.bstats:bstats-bukkit:2.2.1")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    processResources {
        filteringCharset = "UTF-8"
        filter<org.apache.tools.ant.filters.ReplaceTokens>(
            "tokens" to mapOf(
                Pair("name", project.name),
                Pair("description", "A simple animated scoreboard plugin for your server."),
                Pair("url", "https://r4g3baby.com"),
                Pair("package", "${project.group}.${project.name.toLowerCase()}"),
                Pair("version", project.version)
            )
        )
    }

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")

        val shaded = "${project.group}.${project.name.toLowerCase()}.shaded"
        relocate("org.codemc.worldguardwrapper", "$shaded.worldguardwrapper")
        relocate("net.swiftzer.semver", "$shaded.semver")
        relocate("org.bstats", "$shaded.bstats")
        relocate("org.jetbrains", "$shaded.jetbrains")
        relocate("org.intellij", "$shaded.intellij")
        relocate("kotlin", "$shaded.kotlin")
    }
}