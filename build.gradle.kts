plugins {
    kotlin("jvm") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.r4g3baby"
version = "3.7.0"

repositories {
    jcenter()

    maven(uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots"))
    maven(uri("https://repo.dmulloy2.net/nexus/repository/public/"))
    maven(uri("https://repo.extendedclip.com/content/repositories/placeholderapi/"))
    maven(uri("https://repo.codemc.org/repository/maven-public/"))
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compileOnly("org.bukkit:bukkit:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.1")
    compileOnly("me.clip:placeholderapi:2.8.2")

    implementation("org.codemc.worldguardwrapper:worldguardwrapper:1.2.0-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:2.2.1")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
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

        relocate("org.codemc.worldguardwrapper", "com.r4g3baby.simplescore.shaded.worldguardwrapper")
        relocate("org.bstats", "com.r4g3baby.simplescore.shaded.bstats")
        relocate("org.jetbrains", "com.r4g3baby.simplescore.shaded.jetbrains")
        relocate("org.intellij", "com.r4g3baby.simplescore.shaded.intellij")
        relocate("kotlin", "com.r4g3baby.simplescore.shaded.kotlin")
    }
}