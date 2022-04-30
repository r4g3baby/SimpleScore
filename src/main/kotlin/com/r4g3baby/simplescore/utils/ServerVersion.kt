package com.r4g3baby.simplescore.utils

import org.bukkit.Bukkit
import java.util.regex.Pattern

class ServerVersion : Comparable<ServerVersion> {
    companion object {
        private val VERSION_PATTERN = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-.]+).*")

        var currentVersion = ServerVersion(0, 0, 0)
            private set

        init {
            val serverVersion = Bukkit.getVersion()
            val version = VERSION_PATTERN.matcher(serverVersion)
            if (version.matches() && version.group(1) != null) {
                currentVersion = ServerVersion(version.group(1))
            } else throw IllegalStateException("Cannot parse version '$serverVersion'")
        }

        fun isAbove(version: ServerVersion): Boolean {
            return currentVersion.isAbove(version)
        }

        fun atOrAbove(version: ServerVersion): Boolean {
            return currentVersion.atOrAbove(version)
        }
    }

    private val major: Int
    private val minor: Int
    private val build: Int

    constructor(major: Int, minor: Int, build: Int) {
        this.major = major
        this.minor = minor
        this.build = build
    }

    constructor(version: String) {
        val numbers = parseVersion(version)
        this.major = numbers[0]
        this.minor = numbers[1]
        this.build = numbers[2]
    }

    fun isAbove(): Boolean {
        return Companion.isAbove(this)
    }

    fun atOrAbove(): Boolean {
        return Companion.atOrAbove(this)
    }

    fun isAbove(other: ServerVersion): Boolean {
        return compareTo(other) > 0
    }

    fun atOrAbove(other: ServerVersion): Boolean {
        return compareTo(other) >= 0
    }

    override fun compareTo(other: ServerVersion): Int {
        major.compareTo(other.major).takeIf { it != 0 }?.let { return it }
        minor.compareTo(other.minor).takeIf { it != 0 }?.let { return it }
        build.compareTo(other.build).takeIf { it != 0 }?.let { return it }
        return 0
    }

    private fun parseVersion(version: String): IntArray {
        val elements = version.split(".").toTypedArray()
        val numbers = IntArray(3)

        check(elements.isNotEmpty()) { "Invalid MC version: $version" }

        for (i in 0 until numbers.size.coerceAtMost(elements.size)) {
            numbers[i] = elements[i].trim { it <= ' ' }.toInt()
        }
        return numbers
    }
}