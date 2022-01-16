package com.r4g3baby.simplescore.scoreboard.handlers

import com.r4g3baby.simplescore.utils.ServerVersion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot

class BukkitScoreboard : ScoreboardHandler() {
    private val afterAquaticUpdate = ServerVersion("1.13").atOrAbove()

    override val titleLengthLimit = if (afterAquaticUpdate) 128 else 32
    override val teamLengthLimit = titleLengthLimit / 2

    override fun createScoreboard(player: Player) {
        if (player.scoreboard != null && player.scoreboard != Bukkit.getScoreboardManager().mainScoreboard) {
            player.scoreboard.getObjective(getPlayerIdentifier(player))?.unregister()
        } else player.scoreboard = Bukkit.getScoreboardManager().newScoreboard

        val objective = player.scoreboard.registerNewObjective(getPlayerIdentifier(player), "dummy")
        objective.displaySlot = DisplaySlot.SIDEBAR
    }

    override fun removeScoreboard(player: Player) {
        player.scoreboard?.teams?.forEach { it.unregister() }
        player.scoreboard?.getObjective(getPlayerIdentifier(player))?.unregister()
    }

    override fun clearScoreboard(player: Player) {
        val objective = player.scoreboard?.getObjective(getPlayerIdentifier(player))
        if (objective != null && objective.isModifiable) {
            objective.scoreboard.entries.forEach { scoreName ->
                objective.scoreboard.getTeam(scoreName).unregister()
                objective.scoreboard.resetScores(scoreName)
            }
        }
    }

    override fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player) {
        val objective = player.scoreboard?.getObjective(getPlayerIdentifier(player))
        if (objective != null && objective.isModifiable) {
            if (objective.displayName != title) {
                if (title.length > titleLengthLimit) {
                    objective.displayName = title.substring(0, titleLengthLimit)
                } else objective.displayName = title
            }

            scores.forEach { (score, value) ->
                val scoreName = scoreToName(score)

                var team = objective.scoreboard.getTeam(scoreName)
                if (team == null) {
                    team = objective.scoreboard.registerNewTeam(scoreName)
                    team.addEntry(scoreName)
                }

                val splitText = splitScoreLine(value)
                team.prefix = splitText.first
                team.suffix = splitText.second

                val objScore = objective.getScore(scoreName)
                if (objScore.score != score) {
                    objScore.score = score
                }
            }

            objective.scoreboard.entries.forEach { scoreName ->
                val score = objective.getScore(scoreName).score
                if (!scores.containsKey(score)) {
                    objective.scoreboard.getTeam(scoreName).unregister()
                    objective.scoreboard.resetScores(scoreName)
                }
            }
        }
    }

    override fun hasScoreboard(player: Player): Boolean {
        val objective = player.scoreboard?.getObjective(getPlayerIdentifier(player))
        return objective != null && objective.isModifiable
    }
}