package com.r4g3baby.simplescore.scoreboard.listeners

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.WorldGuardAPI
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*

class PlayersListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        SimpleScore.scoreboardManager.createScoreboard(e.player)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        SimpleScore.scoreboardManager.removeScoreboard(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(e: PlayerChangedWorldEvent) {
        SimpleScore.scoreboardManager.clearScoreboard(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(e: PlayerMoveEvent) {
        if (e.from.blockX == e.to.blockX && e.from.blockY == e.to.blockY && e.from.blockZ == e.to.blockZ) return
        if (e.from.world != e.to.world) return

        if (WorldGuardAPI.isEnabled) {
            val fromFlag = WorldGuardAPI.getFlag(e.player, e.from)
            val toFlag = WorldGuardAPI.getFlag(e.player, e.to)

            if (!fromFlag.isEqual(toFlag)) {
                Bukkit.getScheduler().runTask(SimpleScore.plugin) {
                    SimpleScore.scoreboardManager.clearScoreboard(e.player)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (e.from.world != e.to.world) return

        if (WorldGuardAPI.isEnabled) {
            val fromFlag = WorldGuardAPI.getFlag(e.player, e.from)
            val toFlag = WorldGuardAPI.getFlag(e.player, e.to)

            if (!fromFlag.isEqual(toFlag)) {
                Bukkit.getScheduler().runTask(SimpleScore.plugin) {
                    SimpleScore.scoreboardManager.clearScoreboard(e.player)
                }
            }
        }
    }
}