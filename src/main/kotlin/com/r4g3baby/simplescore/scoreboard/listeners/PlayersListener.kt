package com.r4g3baby.simplescore.scoreboard.listeners

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.WorldGuardAPI
import com.r4g3baby.simplescore.utils.isEqual
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*

class PlayersListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (
            SimpleScore.scoreboardManager.getWorldScoreboards(e.player.world).isNotEmpty()
            || WorldGuardAPI.getFlag(e.player, e.player.location).isNotEmpty()
        ) {
            SimpleScore.scoreboardManager.createScoreboard(e.player)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        if (
            SimpleScore.scoreboardManager.getWorldScoreboards(e.player.world).isNotEmpty()
            || WorldGuardAPI.getFlag(e.player, e.player.location).isNotEmpty()
        ) {
            SimpleScore.scoreboardManager.removeScoreboard(e.player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(e: PlayerChangedWorldEvent) {
        clearIfNeeded(e.player, null, e.player.location)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(e: PlayerMoveEvent) {
        if (e.from.blockX == e.to.blockX && e.from.blockY == e.to.blockY && e.from.blockZ == e.to.blockZ) return
        if (e.from.world != e.to.world) return

        clearIfNeeded(e.player, e.from, e.to)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (e.from.world != e.to.world) return

        clearIfNeeded(e.player, e.from, e.to)
    }

    private fun clearIfNeeded(player: Player, from: Location?, to: Location) {
        val toBoards = SimpleScore.scoreboardManager.getWorldScoreboards(to.world)
        val toFlag = WorldGuardAPI.getFlag(player, to)
        if (SimpleScore.scoreboardManager.hasScoreboard(player)) {
            if (toBoards.isEmpty() && toFlag.isEmpty()) {
                SimpleScore.scoreboardManager.removeScoreboard(player)
            } else if (from != null) {
                val fromBoards = SimpleScore.scoreboardManager.getWorldScoreboards(from.world)
                val fromFlag = WorldGuardAPI.getFlag(player, from)
                if (!fromBoards.isEqual(toBoards) || !fromFlag.isEqual(toFlag)) {
                    Bukkit.getScheduler().runTask(SimpleScore.plugin) {
                        SimpleScore.scoreboardManager.clearScoreboard(player)
                    }
                }
            }
        } else if (toBoards.isNotEmpty() || toFlag.isNotEmpty()) {
            SimpleScore.scoreboardManager.createScoreboard(player)
        }
    }
}