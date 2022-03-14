package com.r4g3baby.simplescore.scoreboard.listeners

import com.gmail.nossr50.events.scoreboard.McMMOScoreboardObjectiveEvent
import com.gmail.nossr50.events.scoreboard.McMMOScoreboardRevertEvent
import com.gmail.nossr50.mcMMO
import com.r4g3baby.simplescore.SimpleScore
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class McMMOListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onMcMMOScoreboardObjective(e: McMMOScoreboardObjectiveEvent) {
        SimpleScore.manager.playersData.setDisabled(mcMMO.p, e.targetPlayer, true)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onMcMMOScoreboardRevert(e: McMMOScoreboardRevertEvent) {
        SimpleScore.manager.playersData.setDisabled(mcMMO.p, e.targetPlayer, false)
    }

    // EventPriority set to low, so it doesn't get called after the onPlayerQuit event from PlayersListener
    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        SimpleScore.manager.playersData.setDisabled(mcMMO.p, e.player, false)
    }
}