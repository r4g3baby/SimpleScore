package com.r4g3baby.simplescore.scoreboard.handlers

import com.r4g3baby.simplescore.SimpleScore
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot

class BukkitScoreboard(private val plugin: SimpleScore) : ScoreboardHandler() {
    override fun createScoreboard(player: Player) {
        if (player.scoreboard != null && player.scoreboard != plugin.server.scoreboardManager.mainScoreboard) {
            player.scoreboard.getObjective(getPlayerIdentifier(player))?.unregister()
        } else player.scoreboard = plugin.server.scoreboardManager.newScoreboard

        val objective = player.scoreboard.registerNewObjective(getPlayerIdentifier(player), "dummy")
        objective.displaySlot = DisplaySlot.SIDEBAR
    }

    override fun removeScoreboard(player: Player) {
        player.scoreboard?.getObjective(getPlayerIdentifier(player))?.unregister()
    }

    override fun clearScoreboard(player: Player) {
        val objective = player.scoreboard?.getObjective(getPlayerIdentifier(player))
        if (objective != null && objective.isModifiable) {
            objective.scoreboard.entries.forEach { objective.scoreboard.resetScores(it) }
        }
    }

    override fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player) {
        val objective = player.scoreboard?.getObjective(getPlayerIdentifier(player))
        if (objective != null && objective.isModifiable) {
            if (objective.displayName != title) {
                objective.displayName = title
            }

            scores.forEach { (score, value) ->
                val objScore = objective.getScore(value)
                if (objScore.score != score) {
                    objScore.score = score
                }
            }

            objective.scoreboard.entries
                .filter { !scores.values.contains(it) }
                .forEach { objective.scoreboard.resetScores(it) }
        }
    }
}