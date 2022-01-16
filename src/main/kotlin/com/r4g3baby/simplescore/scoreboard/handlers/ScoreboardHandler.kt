package com.r4g3baby.simplescore.scoreboard.handlers

import org.bukkit.ChatColor
import org.bukkit.entity.Player

abstract class ScoreboardHandler {
    abstract val titleLengthLimit: Int
    abstract val lineLengthLimit: Int

    abstract fun createScoreboard(player: Player)
    abstract fun removeScoreboard(player: Player)
    abstract fun clearScoreboard(player: Player)
    abstract fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player)
    abstract fun hasScoreboard(player: Player): Boolean

    protected fun getPlayerIdentifier(player: Player): String {
        return "sb${player.uniqueId.toString().replace("-", "")}".substring(0..15)
    }

    protected fun scoreToName(score: Int): String {
        return score.toString().toCharArray()
            .joinToString(ChatColor.COLOR_CHAR.toString(), ChatColor.COLOR_CHAR.toString())
    }

    protected fun splitScoreLine(text: String): Pair<String, String> {
        val maxLength = lineLengthLimit / 2
        if (text.length > maxLength) {
            val index = if (text.elementAt(maxLength - 1) == ChatColor.COLOR_CHAR) maxLength - 1 else maxLength

            val prefix = text.substring(0, index)
            val lastColors = ChatColor.getLastColors(prefix)

            var suffix = lastColors + text.substring(index)
            if (suffix.length > maxLength) {
                suffix = suffix.substring(0, maxLength)
            }

            return prefix to suffix
        }
        return text to ""
    }
}