package com.r4g3baby.simplescore.scoreboard.listeners

import com.r4g3baby.simplescore.SimpleScore
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*

class PlayerListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onAsyncPlayerPreLogin(e: AsyncPlayerPreLoginEvent) {
        SimpleScore.manager.playersData.loadPlayer(e.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        SimpleScore.manager.updateScoreboardState(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        SimpleScore.manager.scoreboardHandler.removeScoreboard(e.player)
        Bukkit.getScheduler().runTaskAsynchronously(SimpleScore.plugin) {
            SimpleScore.manager.playersData.unloadPlayer(e.player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerKick(e: PlayerKickEvent) {
        SimpleScore.manager.scoreboardHandler.removeScoreboard(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(e: PlayerChangedWorldEvent) {
        SimpleScore.manager.updateScoreboardState(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(e: PlayerMoveEvent) {
        if (e.from.blockX == e.to.blockX && e.from.blockY == e.to.blockY && e.from.blockZ == e.to.blockZ) return

        SimpleScore.manager.updateScoreboardState(e.player, e.to)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (e.from.world != e.to.world) return

        SimpleScore.manager.updateScoreboardState(e.player, e.to)
    }
}