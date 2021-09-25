package com.r4g3baby.simplescore.scoreboard.tasks

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.placeholders.PlaceholderReplacer
import com.r4g3baby.simplescore.scoreboard.placeholders.VariablesReplacer
import com.r4g3baby.simplescore.scoreboard.worldguard.WorldGuardAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.translateAlternateColorCodes
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.regex.Pattern

class ScoreboardTask : BukkitRunnable() {
    override fun run() {
        SimpleScore.scoreboardManager.scoreboards.forEach { (_, scoreboard) ->
            scoreboard.titles.next()
            scoreboard.scores.forEach { (_, value) ->
                value.next()
            }
        }

        val playerBoards = HashMap<Player, Pair<String, HashMap<Int, String>>>()
        for (world in Bukkit.getWorlds()) {
            val players = world.players.filter {
                // No need to waste power computing scoreboards for players that won't see it
                val playerData = SimpleScore.scoreboardManager.playersData.get(it)
                return@filter !playerData.isHidden && !playerData.isDisabled
            }.toMutableList()
            if (players.size == 0) continue

            if (WorldGuardAPI.isEnabled) {
                val iterator = players.iterator()
                iterator.forEach { player ->
                    val flag = WorldGuardAPI.getFlag(player)
                    if (flag.isNotEmpty()) {
                        for (boardName in flag) {
                            val regionBoard = SimpleScore.scoreboardManager.scoreboards.get(boardName)
                            if (regionBoard != null && regionBoard.canSee(player)) {
                                val title = regionBoard.titles.current()
                                val scores = HashMap<Int, String>()
                                regionBoard.scores.forEach { (score, value) ->
                                    scores[score] = value.current()
                                }

                                playerBoards[player] = if (SimpleScore.config.asyncPlaceholders) {
                                    applyPlaceholders(title, scores, player)
                                } else (title to scores)
                                iterator.remove()
                                break
                            }
                        }
                    }
                }
            }

            SimpleScore.scoreboardManager.scoreboards.getForWorld(world).forEach { worldBoard ->
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
                            applyPlaceholders(title, scores, player)
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
                        val tmp = applyPlaceholders(board.first, board.second, player)
                        applyVariables(tmp.first, tmp.second, player)
                    } else applyVariables(board.first, board.second, player)
                    SimpleScore.scoreboardManager.updateScoreboard(updatedBoard.first, updatedBoard.second, player)
                }
            }
        }
    }

    private fun applyPlaceholders(title: String, scores: HashMap<Int, String>, player: Player): Pair<String, HashMap<Int, String>> {
        val toDisplayTitle = replacePlaceholders(title, player)

        val toDisplayScores = HashMap<Int, String>()
        scores.forEach { (score, ogValue) ->
            toDisplayScores[score] = replacePlaceholders(ogValue, player)
        }

        return (toDisplayTitle to toDisplayScores)
    }

    private fun replacePlaceholders(input: String, player: Player): String {
        val result = PlaceholderReplacer.replace(input, player)
        return translateHexColorCodes(translateAlternateColorCodes('&', result))
    }

    private fun applyVariables(title: String, scores: HashMap<Int, String>, player: Player): Pair<String, HashMap<Int, String>> {
        var toDisplayTitle: String
        val toDisplayScores = HashMap<Int, String>()

        toDisplayTitle = VariablesReplacer.replace(title, player)
        if (SimpleScore.scoreboardManager.hasLineLengthLimit() && toDisplayTitle.length > 32) {
            toDisplayTitle = toDisplayTitle.substring(0..31)
        }

        scores.forEach { (score, ogValue) ->
            var value = preventDuplicates(VariablesReplacer.replace(ogValue, player), toDisplayScores.values)
            if (SimpleScore.scoreboardManager.hasLineLengthLimit() && value.length > 40) {
                value = value.substring(0..39)
            }
            toDisplayScores[score] = value
        }

        return (toDisplayTitle to toDisplayScores)
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