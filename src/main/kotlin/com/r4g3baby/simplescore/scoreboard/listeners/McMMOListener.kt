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
    @EventHandler(priority = EventPriority.MONITOR)
    fun onMcMMOScoreboardObjective(e: McMMOScoreboardObjectiveEvent) {
        SimpleScore.scoreboardManager.playersData.get(e.targetPlayer).disable(mcMMO.p)
        SimpleScore.scoreboardManager.removeScoreboard(e.targetPlayer)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onMcMMOScoreboardRevert(e: McMMOScoreboardRevertEvent) {
        SimpleScore.scoreboardManager.playersData.get(e.targetPlayer).enable(mcMMO.p)
        SimpleScore.scoreboardManager.createScoreboard(e.targetPlayer)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        SimpleScore.scoreboardManager.playersData.get(e.player).enable(mcMMO.p)
    }
}