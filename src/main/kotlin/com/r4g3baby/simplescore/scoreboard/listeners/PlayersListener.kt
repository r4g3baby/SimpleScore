package com.r4g3baby.simplescore.scoreboard.listeners

import com.r4g3baby.simplescore.SimpleScore
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayersListener(private val plugin: SimpleScore) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        plugin.scoreboardManager.scoreboardHandler.createScoreboard(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        plugin.scoreboardManager.scoreboardHandler.removeScoreboard(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(e: PlayerChangedWorldEvent) {
        if (plugin.scoreboardManager.hasScoreboard(e.player.world)) {
            plugin.scoreboardManager.scoreboardHandler.createScoreboard(e.player)
        } else {
            plugin.scoreboardManager.scoreboardHandler.removeScoreboard(e.player)
        }
    }
}