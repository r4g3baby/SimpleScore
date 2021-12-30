package com.r4g3baby.simplescore.scoreboard.tasks

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
import com.r4g3baby.simplescore.scoreboard.placeholders.PlaceholderReplacer
import com.r4g3baby.simplescore.scoreboard.placeholders.VariablesReplacer
import com.r4g3baby.simplescore.scoreboard.worldguard.WorldGuardAPI
import com.r4g3baby.simplescore.utils.translateHexColorCodes
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.translateAlternateColorCodes
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class ScoreboardTask : BukkitRunnable() {
    override fun run() {
        SimpleScore.manager.scoreboards.forEach { (_, scoreboard) ->
            scoreboard.titles.tick()
            scoreboard.scores.forEach { score ->
                score.frames.tick()
            }
        }

        val playerBoards = HashMap<Player, Pair<String, Map<Int, String>>>()
        for (world in Bukkit.getWorlds()) {
            val players = world.players.filter { player ->
                val playerData = SimpleScore.manager.playersData.get(player)
                // No need to waste power computing scoreboards for players that won't see it
                if (playerData.isHidden || playerData.isDisabled) return@filter false

                // Check if player has a valid scoreboard and if so use it
                playerData.scoreboards.forEach { scoreboard ->
                    if (scoreboard.canSee(player)) {
                        playerBoards[player] = getPlayerBoard(scoreboard, player)
                        return@filter false
                    }
                }

                // Player scoreboards override world scoreboards
                return@filter !playerData.hasScoreboards
            }.toMutableList()
            if (players.size == 0) continue

            if (WorldGuardAPI.isEnabled) {
                val iterator = players.iterator()
                iterator.forEach { player ->
                    val flag = WorldGuardAPI.getFlag(player)
                    if (flag.isNotEmpty()) {
                        for (boardName in flag) {
                            val regionBoard = SimpleScore.manager.scoreboards.get(boardName)
                            if (regionBoard != null && regionBoard.canSee(player)) {
                                playerBoards[player] = getPlayerBoard(regionBoard, player)
                                break
                            }
                        }

                        // Region boards override world boards
                        iterator.remove()
                    }
                }
            }

            SimpleScore.manager.scoreboards.getForWorld(world).forEach { worldBoard ->
                if (players.size == 0) return@forEach

                val iterator = players.iterator()
                iterator.forEach { player ->
                    if (worldBoard.canSee(player)) {
                        playerBoards[player] = getPlayerBoard(worldBoard, player)
                        iterator.remove()
                    }
                }
            }
        }

        Bukkit.getScheduler().runTask(SimpleScore.plugin) {
            playerBoards.filter { it.key.isOnline }.forEach { (player, board) ->
                val playerData = SimpleScore.manager.playersData.get(player)
                // Check if our player didn't hide/disable the scoreboard
                if (!playerData.isHidden && !playerData.isDisabled) {
                    val updatedBoard = if (!SimpleScore.config.asyncPlaceholders) {
                        val tmp = applyPlaceholders(board.first, board.second, player)
                        applyVariables(tmp.first, tmp.second, player)
                    } else applyVariables(board.first, board.second, player)

                    with(SimpleScore.manager.scoreboardHandler) {
                        updateScoreboard(updatedBoard.first, updatedBoard.second, player)
                    }
                }
            }
        }
    }

    private fun getPlayerBoard(scoreboard: Scoreboard, player: Player): Pair<String, Map<Int, String>> {
        val title = scoreboard.titles.current?.text ?: ""
        val scores = HashMap<Int, String>()
        scoreboard.scores.forEach { score ->
            if (score.canSee(player)) {
                scores[score.score] = score.frames.current?.text ?: ""
            }
        }
        return if (SimpleScore.config.asyncPlaceholders) {
            applyPlaceholders(title, scores, player)
        } else (title to scores)
    }

    private fun applyPlaceholders(title: String, scores: Map<Int, String>, player: Player): Pair<String, Map<Int, String>> {
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

    private fun applyVariables(title: String, scores: Map<Int, String>, player: Player): Pair<String, Map<Int, String>> {
        var toDisplayTitle: String
        val toDisplayScores = HashMap<Int, String>()

        toDisplayTitle = VariablesReplacer.replace(title, player)
        if (SimpleScore.manager.scoreboardHandler.hasLineLengthLimit() && toDisplayTitle.length > 32) {
            toDisplayTitle = toDisplayTitle.substring(0..31)
        }

        scores.forEach { (score, ogValue) ->
            var value = preventDuplicates(VariablesReplacer.replace(ogValue, player), toDisplayScores.values)
            if (SimpleScore.manager.scoreboardHandler.hasLineLengthLimit() && value.length > 40) {
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
}