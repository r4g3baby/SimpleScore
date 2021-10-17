package com.r4g3baby.simplescore.scoreboard.listeners

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.worldguard.WorldGuardAPI
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
            SimpleScore.manager.scoreboards.getForWorld(e.player.world).isNotEmpty()
            || WorldGuardAPI.getFlag(e.player, e.player.location).isNotEmpty()
            || SimpleScore.manager.playersData.hasScoreboard(e.player)
        ) {
            SimpleScore.manager.createScoreboard(e.player)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        SimpleScore.manager.removeScoreboard(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(e: PlayerChangedWorldEvent) {
        doScoreboardCheck(e.player, null, e.player.location)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(e: PlayerMoveEvent) {
        if (e.from.blockX == e.to.blockX && e.from.blockY == e.to.blockY && e.from.blockZ == e.to.blockZ) return
        if (e.from.world != e.to.world) return

        doScoreboardCheck(e.player, e.from, e.to)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (e.from.world != e.to.world) return

        doScoreboardCheck(e.player, e.from, e.to)
    }

    private fun doScoreboardCheck(player: Player, from: Location?, to: Location) {
        val playerData = SimpleScore.manager.playersData.get(player)
        val toBoards = SimpleScore.manager.scoreboards.getForWorld(to.world)
        val toFlag = WorldGuardAPI.getFlag(player, to)
        if (SimpleScore.manager.hasScoreboard(player)) {
            if (playerData.isDisabled || (toBoards.isEmpty() && toFlag.isEmpty() && !playerData.hasScoreboard)) {
                SimpleScore.manager.removeScoreboard(player)
            } else if (from != null) {
                val fromBoards = SimpleScore.manager.scoreboards.getForWorld(from.world)
                val fromFlag = WorldGuardAPI.getFlag(player, from)
                if (!fromBoards.isEqual(toBoards) || !fromFlag.isEqual(toFlag)) {
                    Bukkit.getScheduler().runTask(SimpleScore.plugin) {
                        SimpleScore.manager.clearScoreboard(player)
                    }
                }
            }
        } else if (toBoards.isNotEmpty() || toFlag.isNotEmpty() || playerData.hasScoreboard) {
            SimpleScore.manager.createScoreboard(player)
        }
    }
}