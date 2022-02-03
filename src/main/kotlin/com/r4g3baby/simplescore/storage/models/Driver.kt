package com.r4g3baby.simplescore.storage.models

import java.security.MessageDigest
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes

enum class Driver(
    groupId: String, artifactId: String, version: String, encodedHash: String
) {
    H2(
        "com.h2database", "h2", "1.4.200",
        "OtmsS2qunNnTrBxEdGXh7QYBm4UbiT3WqNdt222FvKY="
    ),
    SQLite(
        "org.xerial", "sqlite-jdbc", "3.36.0.3",
        "rzozdjkeGGoP7WPs1BS3Kogr9FJme0kKC+Or+FtjfT8="
    ),
    MariaDB(
        "org.mariadb.jdbc", "mariadb-java-client", "3.0.3",
        "YTCGoKIPF3389eIn9RknK8a+iL3kAR3g8jxTMjGnrgU="
    ),
    MySQL(
        "mysql", "mysql-connector-java", "8.0.28",
        "oAzN9Tf/UOUAZ7mJEIwiNRl/+2XhlxSbu2aduEPNHD4="
    );

    val fileName: String = "${name.lowercase()}-$version.jar"

    val mavenPath: String = "%s/%s/%s/%s-%s.jar".format(
        groupId.replace(".", "/"), artifactId, version, artifactId, version
    )

    private val hash: ByteArray = Base64.getDecoder().decode(encodedHash)
    fun validateHash(otherHash: ByteArray): Boolean {
        return hash.contentEquals(otherHash)
    }

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