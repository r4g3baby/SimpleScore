package com.r4g3baby.simplescore.scoreboard.tasks

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.WorldGuardAPI
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ScoreboardRunnable(private val plugin: SimpleScore) : BukkitRunnable() {
    override fun run() {
        plugin.scoreboardManager.getScoreboards().forEach { scoreboard ->
            scoreboard.titles.next()
            scoreboard.scores.forEach { (_, value) ->
                value.next()
            }
        }

        for (world in plugin.server.worlds) {
            val players = world.players.filter { !plugin.scoreboardManager.isScoreboardDisabled(it) }.toMutableList()
            if (players.size == 0) continue

            if (plugin.worldGuard) {
                val iterator = players.iterator()
                for (player in iterator) {
                    val flag = WorldGuardAPI.getFlag(player)
                    if (!flag.isNullOrBlank()) {
                        plugin.scoreboardManager.getScoreboard(flag)?.let { regionBoard ->
                            if (regionBoard.canSee(player)) {
                                val title = regionBoard.titles.current()
                                val scores = HashMap<Int, String>()
                                regionBoard.scores.forEach { (score, value) ->
                                    scores[score] = value.current()
                                }

                                sendScoreboard(player, title, scores)
                                iterator.remove()
                            }
                        }
                    }
                }
            }

            plugin.scoreboardManager.getWorldScoreboards(world).forEach { worldBoard ->
                if (players.size == 0) return@forEach

                val title = worldBoard.titles.current()
                val scores = HashMap<Int, String>()
                worldBoard.scores.forEach { (score, value) ->
                    scores[score] = value.current()
                }

                val iterator = players.iterator()
                for (player in iterator) {
                    if (worldBoard.canSee(player)) {
                        sendScoreboard(player, title, scores)
                        iterator.remove()
                    }
                }
            }
        }
    }

    private fun sendScoreboard(player: Player, title: String, scores: HashMap<Int, String>) {
        plugin.server.scheduler.runTask(plugin) {
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

    private fun replaceVariables(text: String, player: Player): String {
        val replacedText = if (plugin.placeholderAPI) {
            PlaceholderAPI.setPlaceholders(player, text)
        } else ChatColor.translateAlternateColorCodes('&', text)

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