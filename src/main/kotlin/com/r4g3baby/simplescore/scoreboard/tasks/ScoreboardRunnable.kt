package com.r4g3baby.simplescore.scoreboard.tasks

import com.r4g3baby.simplescore.SimpleScore
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.roundToInt

class ScoreboardRunnable(private val plugin: SimpleScore) : BukkitRunnable() {
    override fun run() {
        for (world in plugin.server.worlds.filter { plugin.scoreboardManager.hasScoreboard(it) }) {
            val scoreboard = plugin.scoreboardManager.getScoreboard(world)!!

            val title = scoreboard.titles.nextFrame()
            val scores = HashMap<Int, String>()
            for (score in scoreboard.scores.keys) {
                scores[score] = scoreboard.scores.getValue(score).nextFrame()
            }

            world.players.forEach { player ->
                val objective = plugin.scoreboardManager.getObjective(player)
                if (objective != null) {
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

        val hearts = ((player.health / player.maxHealth) * 10).roundToInt()
        return replacedText
                .replace("%online%", plugin.server.onlinePlayers.count().toString())
                .replace("%onworld%", player.world.players.count().toString())
                .replace("%world%", player.world.name)
                .replace("%maxplayers%", plugin.server.maxPlayers.toString())
                .replace("%player%", player.name)
                .replace("%playerdisplayname%", player.displayName)
                .replace("%health%", player.health.roundToInt().toString())
                .replace("%maxhealth%", player.maxHealth.roundToInt().toString())
                .replace("%hearts%", "${ChatColor.DARK_RED}❤".repeat(hearts) + "${ChatColor.GRAY}❤".repeat(10 - hearts))
                .replace("%level%", player.level.toString())
                .replace("%gamemode%", player.gameMode.name.toLowerCase().capitalize())
    }

    private fun preventDuplicates(text: String, values: Collection<String>): String {
        return if (values.contains(text)) {
            preventDuplicates(text + ChatColor.RESET, values)
        } else text
    }
}