package com.r4g3baby.simplescore.scoreboard.listeners

import com.r4g3baby.simplescore.SimpleScore
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

class ScoreboardListener(private val plugin: SimpleScore) : Listener {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (plugin.scoreboardManager != null) {
            if (!plugin.scoreboardManager!!.hasObjective(e.player)) {
                plugin.scoreboardManager!!.createObjective(e.player)
            }
        }
    }

    @EventHandler
    fun onPlayerChangedWorld(e: PlayerChangedWorldEvent) {
        if (plugin.scoreboardManager != null) {
            if (plugin.scoreboardManager!!.hasObjective(e.player)) {
                if (!plugin.scoreboardManager!!.hasScoreboard(e.player.world)) {
                    plugin.scoreboardManager!!.removeObjective(e.player)
                }
            } else if (plugin.scoreboardManager!!.hasScoreboard(e.player.world)) {
                plugin.scoreboardManager!!.removeObjective(e.player)
                plugin.scoreboardManager!!.createObjective(e.player)
            }
        }
    }
}