plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "com.r4g3baby"
version = "3.3.1"

repositories {
    jcenter()

    maven(uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots"))
    maven(uri("https://repo.extendedclip.com/content/repositories/placeholderapi/"))
    maven(uri("https://repo.codemc.org/repository/maven-public/"))
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compileOnly("org.bukkit:bukkit:1.8.8-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.8.2")

    implementation("org.bstats:bstats-bukkit-lite:1.7")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
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

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")

        relocate("org.bstats", "com.r4g3baby.simplescore.shaded.bstats")
    }
}