package com.r4g3baby.simplescore.scoreboard.handlers

import com.r4g3baby.simplescore.SimpleScore
import org.bukkit.ChatColor
import org.bukkit.ChatColor.COLOR_CHAR
import org.bukkit.entity.Player

abstract class ScoreboardHandler {
    companion object {
        fun getPlayerIdentifier(player: Player): String {
            return "sb${player.uniqueId.toString().replace("-", "")}".substring(0..15)
        }
    }

    abstract fun createScoreboard(player: Player)
    abstract fun removeScoreboard(player: Player)
    abstract fun clearScoreboard(player: Player)
    abstract fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player)
    abstract fun hasScoreboard(player: Player): Boolean

    protected fun scoreToName(score: Int): String {
        return score.toString().toCharArray().joinToString(COLOR_CHAR.toString(), COLOR_CHAR.toString())
    }

    protected fun splitScoreLine(text: String, maxSize: Int, cutSuffix: Boolean = true): Pair<String, String> {
        val forceMultiVersionLimit = SimpleScore.config.forceMultiVersion || SimpleScore.isViaBackwardsEnabled
        var index = if (forceMultiVersionLimit) 16 else maxSize
        if (text.length > index) {
            // Prevent spliting normal color codes
            if (text.elementAt(index - 1) == COLOR_CHAR) index--

            // Prevent splitting hex color codes
            for (i in 1..6) {
                val newIndex = index - (i * 2)

                // This isn't a hex color code
                if (text.elementAt(newIndex) != COLOR_CHAR) break

                // Found start of hex color code
                if (text.elementAt(newIndex + 1) == 'x') {
                    index = newIndex
                    break
                }
            }

            val prefix = text.substring(0, index)
            val lastColors = ChatColor.getLastColors(prefix)

            var suffix = lastColors + text.substring(index)
            if (cutSuffix && suffix.length > maxSize) {
                suffix = suffix.substring(0, maxSize)
            }

            return prefix to suffix
        }
        return text to ""
    }
}