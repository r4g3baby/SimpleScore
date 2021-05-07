package com.r4g3baby.simplescore.scoreboard.tasks

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.WorldGuardAPI
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.logging.Level
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ScoreboardTask : BukkitRunnable() {
    override fun run() {
        SimpleScore.scoreboardManager.getScoreboards().forEach { scoreboard ->
            scoreboard.titles.next()
            scoreboard.scores.forEach { (_, value) ->
                value.next()
            }
        }

        val playerBoards = HashMap<Player, Pair<String, HashMap<Int, String>>>()
        for (world in Bukkit.getWorlds()) {
            val players = world.players.filter {
                !SimpleScore.scoreboardManager.isScoreboardDisabled(it)
            }.toMutableList()
            if (players.size == 0) continue

            if (WorldGuardAPI.isEnabled) {
                val iterator = players.iterator()
                iterator.forEach { player ->
                    val flag = WorldGuardAPI.getFlag(player)
                    if (!flag.isNullOrEmpty()) {
                        for (boardName in flag) {
                            val regionBoard = SimpleScore.scoreboardManager.getScoreboard(boardName)
                            if (regionBoard != null && regionBoard.canSee(player)) {
                                val title = regionBoard.titles.current()
                                val scores = HashMap<Int, String>()
                                regionBoard.scores.forEach { (score, value) ->
                                    scores[score] = value.current()
                                }

                                playerBoards[player] = if (SimpleScore.config.asyncPlaceholders) {
                                    applyPlaceholders(player, title, scores)
                                } else (title to scores)
                                iterator.remove()
                                break
                            }
                        }
                    }
                }
            }

            SimpleScore.scoreboardManager.getWorldScoreboards(world).forEach { worldBoard ->
                if (players.size == 0) return@forEach

                val title = worldBoard.titles.current()
                val scores = HashMap<Int, String>()
                worldBoard.scores.forEach { (score, value) ->
                    scores[score] = value.current()
                }

                val iterator = players.iterator()
                iterator.forEach { player ->
                    if (worldBoard.canSee(player)) {
                        playerBoards[player] = if (SimpleScore.config.asyncPlaceholders) {
                            applyPlaceholders(player, title, scores)
                        } else (title to scores)
                        iterator.remove()
                    }
                }
            }
        }

        Bukkit.getScheduler().runTask(SimpleScore.plugin) {
            playerBoards.forEach { (player, board) ->
                if (player.isOnline) {
                    val updatedBoard = if (!SimpleScore.config.asyncPlaceholders) {
                        val tmp = applyPlaceholders(player, board.first, board.second)
                        applyVariables(player, tmp.first, tmp.second)
                    } else applyVariables(player, board.first, board.second)
                    SimpleScore.scoreboardManager.updateScoreboard(updatedBoard.first, updatedBoard.second, player)
                }
            }
        }
    }

    private fun applyPlaceholders(player: Player, title: String, scores: HashMap<Int, String>): Pair<String, HashMap<Int, String>> {
        val toDisplayTitle = replacePlaceholders(title, player)

        val toDisplayScores = HashMap<Int, String>()
        scores.forEach { (score, ogValue) ->
            toDisplayScores[score] = replacePlaceholders(ogValue, player)
        }

        return (toDisplayTitle to toDisplayScores)
    }

    private var lastException = System.currentTimeMillis()
    private fun replacePlaceholders(text: String, player: Player): String {
        return translateHexColorCodes(
            if (SimpleScore.usePlaceholderAPI) {
                try {
                    PlaceholderAPI.setPlaceholders(player, text)
                } catch (ex: Exception) {
                    if ((System.currentTimeMillis() - lastException) > 5 * 1000) {
                        lastException = System.currentTimeMillis()
                        SimpleScore.plugin.logger.log(
                            Level.WARNING, if (SimpleScore.config.asyncPlaceholders) {
                                "Could not apply PlaceholderAPI placeholders. Disable 'AsyncPlaceholders' and try again"
                            } else "Could not apply PlaceholderAPI placeholders", ex
                        )
                    }
                    ChatColor.translateAlternateColorCodes('&', text)
                }
            } else ChatColor.translateAlternateColorCodes('&', text)
        )
    }

    private fun applyVariables(player: Player, title: String, scores: HashMap<Int, String>): Pair<String, HashMap<Int, String>> {
        var toDisplayTitle: String
        val toDisplayScores = HashMap<Int, String>()

        toDisplayTitle = replaceVariables(title, player)
        if (SimpleScore.scoreboardManager.hasLineLengthLimit() && toDisplayTitle.length > 32) {
            toDisplayTitle = toDisplayTitle.substring(0..31)
        }

        scores.forEach { (score, ogValue) ->
            var value = preventDuplicates(replaceVariables(ogValue, player), toDisplayScores.values)
            if (SimpleScore.scoreboardManager.hasLineLengthLimit() && value.length > 40) {
                value = value.substring(0..39)
            }
            toDisplayScores[score] = value
        }

        return (toDisplayTitle to toDisplayScores)
    }

    private fun replaceVariables(text: String, player: Player): String {
        val hearts = min(10, max(0, ((player.health / player.maxHealth) * 10).roundToInt()))
        return text
            .replace("%online%", Bukkit.getOnlinePlayers().count().toString())
            .replace("%onworld%", player.world.players.count().toString())
            .replace("%world%", player.world.name)
            .replace("%maxplayers%", Bukkit.getMaxPlayers().toString())
            .replace("%player%", player.name)
            .replace("%playerdisplayname%", player.displayName)
            .replace("%health%", player.health.roundToInt().toString())
            .replace("%maxhealth%", player.maxHealth.roundToInt().toString())
            .replace("%hearts%", "${ChatColor.DARK_RED}❤".repeat(hearts) + "${ChatColor.GRAY}❤".repeat(10 - hearts))
            .replace("%level%", player.level.toString())
            .replace("%gamemode%", player.gameMode.name.lowercase().replaceFirstChar { it.uppercase() })
    }

    private fun preventDuplicates(text: String, values: Collection<String>): String {
        return if (values.contains(text)) {
            preventDuplicates(text + ChatColor.RESET, values)
        } else text
    }

    private val hexPattern: Pattern = Pattern.compile("&?#([A-Fa-f0-9]{6})")
    private fun translateHexColorCodes(text: String): String {
        val matcher = hexPattern.matcher(text)
        val buffer = StringBuffer(text.length + 4 * 8)
        while (matcher.find()) {
            val group = matcher.group(1)
            matcher.appendReplacement(
                buffer, ChatColor.COLOR_CHAR.toString() + "x"
                    + ChatColor.COLOR_CHAR + group[0] + ChatColor.COLOR_CHAR + group[1]
                    + ChatColor.COLOR_CHAR + group[2] + ChatColor.COLOR_CHAR + group[3]
                    + ChatColor.COLOR_CHAR + group[4] + ChatColor.COLOR_CHAR + group[5]
            )
        }
        return matcher.appendTail(buffer).toString()
    }
}