package com.r4g3baby.simplescore.scoreboard.listeners

import com.r4g3baby.simplescore.SimpleScore
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*

class PlayersListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        SimpleScore.manager.updateScoreboardState(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        try {
            SimpleScore.manager.scoreboardHandler.removeScoreboard(e.player)
        } catch (ex: IllegalArgumentException) {
            // For some strange reason the channel will already be closed when a player is kicked
            if (ex.message != "cannot send packets to a closed channel") {
                throw ex
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(e: PlayerChangedWorldEvent) {
        SimpleScore.manager.updateScoreboardState(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(e: PlayerMoveEvent) {
        if (e.from.blockX == e.to.blockX && e.from.blockY == e.to.blockY && e.from.blockZ == e.to.blockZ) return

        SimpleScore.manager.updateScoreboardState(e.player, e.to, e.from)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (e.from.world != e.to.world) return

        SimpleScore.manager.updateScoreboardState(e.player, e.to, e.from)
    }
}