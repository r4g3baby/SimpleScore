package com.r4g3baby.simplescore.storage.models

enum class Driver(
    groupId: String,
    artifactId: String,
    version: String
) {
    H2("com.h2database", "h2", "1.4.200"),
    SQLite("org.xerial", "sqlite-jdbc", "3.36.0.3");

    val fileName: String = "${name.lowercase()}-$version.jar"

    val mavenPath: String = "%s/%s/%s/%s-%s.jar".format(
        groupId.replace(".", "/"), artifactId, version, artifactId, version
    )

    companion object {
        @JvmStatic
        fun fromValue(value: String): Driver? {
            for (driver in values()) {
                if (driver.name.equals(value, ignoreCase = true)) {
                    return driver
                }
            }
            return null
        }
    }
}