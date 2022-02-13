package com.r4g3baby.simplescore.scoreboard.placeholders

import com.r4g3baby.simplescore.utils.lazyReplace
import org.bukkit.Bukkit
import org.bukkit.ChatColor.DARK_RED
import org.bukkit.ChatColor.GRAY
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object VariablesReplacer {
    fun replace(input: String, player: Player): String {
        return input.lazyReplace("%online%") {
            Bukkit.getOnlinePlayers().count().toString()
        }.lazyReplace("%onworld%") {
            player.world.players.count().toString()
        }.lazyReplace("%world%") {
            player.world.name
        }.lazyReplace("%maxplayers%") {
            Bukkit.getMaxPlayers().toString()
        }.lazyReplace("%player%") {
            player.name
        }.lazyReplace("%displayname%") {
            player.displayName
        }.lazyReplace("%health%") {
            player.health.roundToInt().toString()
        }.lazyReplace("%maxhealth%") {
            player.maxHealth.roundToInt().toString()
        }.lazyReplace("%hearts%") {
            val hearts = min(10, max(0, ((player.health / player.maxHealth) * 10).roundToInt()))
            "$DARK_RED${"❤".repeat(hearts)}$GRAY${"❤".repeat(10 - hearts)}"
        }.lazyReplace("%level%") {
            player.level.toString()
        }.lazyReplace("%gamemode%") {
            player.gameMode.name.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    fun String.replaceVariables(player: Player): String {
        return replace(this, player)
    }
}