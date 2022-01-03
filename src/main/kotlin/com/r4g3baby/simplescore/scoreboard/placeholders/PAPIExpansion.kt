package com.r4g3baby.simplescore.scoreboard.placeholders

import com.r4g3baby.simplescore.SimpleScore
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class PAPIExpansion(private val plugin: SimpleScore) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return plugin.description.name
    }

    override fun getAuthor(): String {
        return plugin.description.authors[0]
    }

    override fun getVersion(): String {
        return plugin.description.version
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player != null) {
            if (params == "hidden") {
                return SimpleScore.manager.playersData.isHidden(player).toString()
            } else if (params == "disabled") {
                return SimpleScore.manager.playersData.isDisabled(player).toString()
            }
        }
        return null
    }
}