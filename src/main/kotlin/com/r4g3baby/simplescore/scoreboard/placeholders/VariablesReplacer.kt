package com.r4g3baby.simplescore.scoreboard.placeholders

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object VariablesReplacer {
    fun replace(input: String, player: Player): String {
        val hearts = min(10, max(0, ((player.health / player.maxHealth) * 10).roundToInt()))
        return input
            .replace("%online%", Bukkit.getOnlinePlayers().count().toString())
            .replace("%onworld%", player.world.players.count().toString())
            .replace("%world%", player.world.name)
            .replace("%maxplayers%", Bukkit.getMaxPlayers().toString())
            .replace("%player%", player.name)
            .replace("%displayname%", player.displayName)
            .replace("%health%", player.health.roundToInt().toString())
            .replace("%maxhealth%", player.maxHealth.roundToInt().toString())
            .replace("%hearts%", "${ChatColor.DARK_RED}❤".repeat(hearts) + "${ChatColor.GRAY}❤".repeat(10 - hearts))
            .replace("%level%", player.level.toString())
            .replace("%gamemode%", player.gameMode.name.lowercase().replaceFirstChar { it.uppercase() })
    }

    fun String.replaceVariables(player: Player): String {
        return replace(this, player)
    }
}