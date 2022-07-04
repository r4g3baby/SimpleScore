package com.r4g3baby.simplescore.scoreboard.placeholders

import be.maximvdw.placeholderapi.PlaceholderAPI
import be.maximvdw.placeholderapi.PlaceholderReplacer
import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import java.util.*

class PlaceholderProvider {
    init {
        if (SimpleScore.usePlaceholderAPI) {
            object : PlaceholderExpansion() {
                override fun getIdentifier(): String {
                    return SimpleScore.plugin.description.name
                }

                override fun getAuthor(): String {
                    return SimpleScore.plugin.description.authors[0]
                }

                override fun getVersion(): String {
                    return SimpleScore.plugin.description.version
                }

                override fun persist(): Boolean {
                    return true
                }

                override fun onPlaceholderRequest(player: Player?, params: String): String? {
                    if (player != null) {
                        return replaceParams(params, player.uniqueId)
                    }
                    return null
                }
            }.register()
        }

        if (SimpleScore.useMVdWPlaceholderAPI) {
            PlaceholderAPI.registerPlaceholder(SimpleScore.plugin, "simplescore_*", PlaceholderReplacer { e ->
                if (e.offlinePlayer != null) {
                    val params = e.placeholder.substring("simplescore_".length)
                    return@PlaceholderReplacer replaceParams(params, e.offlinePlayer.uniqueId)
                }
                return@PlaceholderReplacer null
            })
        }
    }

    private fun replaceParams(params: String, uniqueId: UUID): String {
        with(SimpleScore.manager.playersData.get(uniqueId) ?: PlayerData(uniqueId)) {
            return when (params) {
                "hidden" -> isHidden.toString()
                "visible" -> (!isHidden).toString()
                "disabled" -> isDisabled.toString()
                "enabled" -> (!isDisabled).toString()
                else -> "invalid params"
            }
        }
    }
}