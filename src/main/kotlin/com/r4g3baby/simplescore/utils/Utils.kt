package com.r4g3baby.simplescore.utils

import org.bukkit.ChatColor
import java.util.regex.Pattern

private val hexPattern: Pattern = Pattern.compile("&?#([A-Fa-f0-9]{6})|\\{#([A-Fa-f0-9]{6})}")
fun translateHexColorCodes(text: String): String {
    val matcher = hexPattern.matcher(text)
    val buffer = StringBuffer(text.length + 4 * 8)
    while (matcher.find()) {
        var group = matcher.group(1)
        if (group == null) group = matcher.group(2)

        matcher.appendReplacement(
            buffer, ChatColor.COLOR_CHAR.toString() + "x"
                + ChatColor.COLOR_CHAR + group[0] + ChatColor.COLOR_CHAR + group[1]
                + ChatColor.COLOR_CHAR + group[2] + ChatColor.COLOR_CHAR + group[3]
                + ChatColor.COLOR_CHAR + group[4] + ChatColor.COLOR_CHAR + group[5]
        )
    }
    return matcher.appendTail(buffer).toString()
}

fun List<Any>.isEqual(other: List<Any>): Boolean {
    if (this.size != other.size) {
        return false
    }

    return this.toTypedArray() contentEquals other.toTypedArray()
}
