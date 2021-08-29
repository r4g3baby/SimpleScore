package com.r4g3baby.simplescore.scoreboard.placeholders

import com.r4g3baby.simplescore.SimpleScore
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class ScoreboardExpansion(private val plugin: SimpleScore) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return plugin.description.name.lowercase()
    }

    override fun getPlugin(): String {
        return plugin.description.name
    }

    override fun getAuthor(): String {
        return plugin.description.authors[0]
    }

    override fun getVersion(): String {
        return plugin.description.version
    }

    override fun onPlaceholderRequest(player: Player?, identifier: String?): String? {
        if (player != null && identifier != null) {
            if (identifier == "enabled") {
                return (!SimpleScore.scoreboardManager.isScoreboardDisabled(player)).toString()
            }
        }
        return null
    }
}