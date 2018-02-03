package com.r4g3baby.simplescore.scoreboard.tasks

import com.r4g3baby.simplescore.SimpleScore
import org.bukkit.ChatColor

class ScoreboardTask(private val plugin: SimpleScore) : Runnable {
    override fun run() {
        if (plugin.scoreboardManager != null) {
            for (world in plugin.server.worlds.filter { plugin.scoreboardManager!!.hasScoreboard(it) }) {
                val scoreboard = plugin.scoreboardManager!!.getScoreboard(world)!!

                val title = scoreboard.titles.poll()
                val scores = HashMap<Int, String>()
                for (score in scoreboard.scores.keys) {
                    val text = scoreboard.scores[score]!!.poll()
                    scores[score] = text
                    scoreboard.scores[score]!!.offer(text)
                }
                scoreboard.titles.offer(title)

                for (player in world.players.filter { plugin.scoreboardManager!!.hasObjective(it) }) {
                    val objective = plugin.scoreboardManager!!.getObjective(player)!!

                    val toDisplayScores = HashMap<Int, String>()
                    for (score in scores.keys) {
                        var value = preventDuplicate(scores[score]!!/*TODO replace placeholders*/, toDisplayScores.values)
                        if (value.length > 40) {
                            value = value.substring(IntRange(0, 40))
                        }
                        toDisplayScores[score] = value
                    }

                    var toDisplayTitle = title // TODO replace placeholders
                    if (toDisplayTitle.length > 32) {
                        toDisplayTitle = toDisplayTitle.substring(IntRange(0, 32))
                    }

                    objective.displayName = toDisplayTitle

                    for (score in toDisplayScores.keys) {
                        val value = toDisplayScores[score]!!
                        if (objective.getScore(value).score != score) {
                            objective.getScore(value).score = score
                        }
                    }

                    objective.scoreboard.entries
                            .filter { !toDisplayScores.values.contains(it) }
                            .forEach { objective.scoreboard.resetScores(it) }
                }
            }
        }
    }

    private fun preventDuplicate(text: String, values: Collection<String>): String {
        if (values.contains(text)) {
            return preventDuplicate(text + ChatColor.RESET, values)
        }
        return text
    }
}