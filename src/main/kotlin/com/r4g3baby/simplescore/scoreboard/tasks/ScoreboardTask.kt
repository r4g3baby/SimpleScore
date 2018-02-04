package com.r4g3baby.simplescore.scoreboard.tasks

import com.r4g3baby.simplescore.SimpleScore
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import kotlin.math.roundToLong

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
                        var value = preventDuplicates(replaceVariables(scores[score]!!, player), toDisplayScores.values)
                        if (value.length > 40) {
                            value = value.substring(IntRange(0, 39))
                        }
                        toDisplayScores[score] = value
                    }

                    var toDisplayTitle = replaceVariables(title, player)
                    if (toDisplayTitle.length > 32) {
                        toDisplayTitle = toDisplayTitle.substring(IntRange(0, 31))
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

    private fun replaceVariables(text: String, player: Player): String {
        var replacedText = ChatColor.translateAlternateColorCodes('&', text)
        if (plugin.placeholderAPI) {
            replacedText = PlaceholderAPI.setPlaceholders(player, replacedText)
        }
        replacedText = replacedText
                .replace("%online%", plugin.server.onlinePlayers.count().toString())
                .replace("%onworld%", player.world.players.count().toString())
                .replace("%world%", player.world.name)
                .replace("%maxplayers%", plugin.server.maxPlayers.toString())
                .replace("%server%", plugin.server.serverName)
                .replace("%player%", player.name)
                .replace("%playerdisplayname%", player.displayName)
                .replace("%health%", player.health.roundToLong().toString())
                .replace("%maxhealth%", player.maxHealth.roundToLong().toString())
                .replace("%level%", player.level.toString())
                .replace("%gamemode%", player.gameMode.name.toLowerCase().capitalize())
        return replacedText
    }

    private fun preventDuplicates(text: String, values: Collection<String>): String {
        if (values.contains(text)) {
            return preventDuplicates(text + ChatColor.RESET, values)
        }
        return text
    }
}