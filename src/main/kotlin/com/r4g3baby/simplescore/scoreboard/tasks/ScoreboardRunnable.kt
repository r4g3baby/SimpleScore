package com.r4g3baby.simplescore.scoreboard.tasks

import com.r4g3baby.simplescore.SimpleScore
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ScoreboardRunnable(private val plugin: SimpleScore) : BukkitRunnable() {
    override fun run() {
        for (world in plugin.server.worlds.filter { plugin.scoreboardManager.hasScoreboard(it) }) {
            val scoreboard = plugin.scoreboardManager.getScoreboard(world)!!

            val title = scoreboard.titles.nextFrame()
            val scores = HashMap<Int, String>()
            scoreboard.scores.forEach { (score, value) ->
                scores[score] = value.nextFrame()
            }

            world.players.forEach { player ->
                var toDisplayTitle: String
                val toDisplayScores = HashMap<Int, String>()

                toDisplayTitle = replaceVariables(title, player)
                if (toDisplayTitle.length > 32) {
                    toDisplayTitle = toDisplayTitle.substring(0..31)
                }

                scores.forEach { (score, ogValue) ->
                    var value = preventDuplicates(replaceVariables(ogValue, player), toDisplayScores.values)
                    if (value.length > 40) {
                        value = value.substring(0..39)
                    }
                    toDisplayScores[score] = value
                }

                plugin.scoreboardManager.updateScoreboard(toDisplayTitle, toDisplayScores, player)
            }
        }
    }

    private fun replaceVariables(text: String, player: Player): String {
        var replacedText = ChatColor.translateAlternateColorCodes('&', text)
        if (plugin.placeholderAPI) {
            replacedText = PlaceholderAPI.setPlaceholders(player, replacedText)
        }

        val hearts = min(10, max(0, ((player.health / player.maxHealth) * 10).roundToInt()))
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