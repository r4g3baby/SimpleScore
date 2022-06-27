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
        SimpleScore.manager.playersData.forEach { (_, playerData) -> playerData.scoreboard?.tick() }

        val possibleBoards = HashMap<Player, List<Scoreboard>>()
        for (world in Bukkit.getWorlds()) {
            val players = world.players.filter { player ->
                // Skip Citizens NPCs
                if (player.hasMetadata("NPC")) return@filter false

                val playerData = SimpleScore.manager.playersData.get(player)
                // No need to waste time computing scoreboards for players that won't see it
                if (playerData.isHidden || playerData.isDisabled) return@filter false

                // Set the list of possible scoreboards for this player
                possibleBoards[player] = playerData.scoreboards.mapNotNull {
                    SimpleScore.manager.scoreboards.get(it)
                }

                // Player scoreboards override world and region scoreboards
                return@filter !playerData.hasScoreboards
            }.toMutableList()
            if (players.size == 0) continue

            if (WorldGuardAPI.isEnabled) {
                val iterator = players.iterator()
                iterator.forEach { player ->
                    val regionBoards = WorldGuardAPI.getFlag(player)
                    if (regionBoards.isNotEmpty()) {
                        // Set the list of possible scoreboards for this player
                        possibleBoards[player] = regionBoards.mapNotNull {
                            SimpleScore.manager.scoreboards.get(it)
                        }

                        // Region boards override world boards
                        iterator.remove()
                    }
                }
            }

            val worldBoards = SimpleScore.manager.scoreboards.getForWorld(world)
            val iterator = players.iterator()
            iterator.forEach { player ->
                possibleBoards[player] = worldBoards
                iterator.remove()
            }
        }

        if (SimpleScore.config.asyncPlaceholders) {
            val playerBoards = HashMap<Player, Pair<String?, Map<Int, String?>>>()
            possibleBoards.forEach { (player, scoreboards) ->
                for (scoreboard in scoreboards) {
                    if (scoreboard.canSee(player)) {
                        playerBoards[player] = getPlayerScoreboard(scoreboard, player)
                        break
                    }
                }
            }

            Bukkit.getScheduler().runTask(SimpleScore.plugin) {
                playerBoards.filter { it.key.isOnline }.forEach { (player, board) ->
                    val playerData = SimpleScore.manager.playersData.get(player)
                    // Check if our player didn't hide/disable the scoreboard
                    if (!playerData.isHidden && !playerData.isDisabled) {
                        with(SimpleScore.manager.scoreboardHandler) {
                            updateScoreboard(board.first, board.second, player)
                        }
                    }
                }
            }
        } else {
            Bukkit.getScheduler().runTask(SimpleScore.plugin) {
                possibleBoards.filter { it.key.isOnline }.forEach { (player, scoreboards) ->
                    val playerData = SimpleScore.manager.playersData.get(player)
                    // Check if our player didn't hide/disable the scoreboard
                    if (!playerData.isHidden && !playerData.isDisabled) {
                        for (scoreboard in scoreboards) {
                            if (scoreboard.canSee(player)) {
                                val board = getPlayerScoreboard(scoreboard, player)
                                with(SimpleScore.manager.scoreboardHandler) {
                                    updateScoreboard(board.first, board.second, player)
                                }
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getPlayerScoreboard(scoreboard: Scoreboard, player: Player): Pair<String?, Map<Int, String?>> {
        val playerScoreboard = SimpleScore.manager.playersData.get(player).let { playerData ->
            if (playerData.scoreboard == null || playerData.scoreboard?.name != scoreboard.name) {
                playerData.scoreboard = scoreboard.asPlayerScoreboard()
            }
            return@let playerData.scoreboard!!
        }

        val title = playerScoreboard.getTitle(player)
        val scores = playerScoreboard.getScores(player)

        // Force a full render of the scoreboard if there are no scores
        val hasNoScores = !SimpleScore.manager.scoreboardHandler.hasScores(player)

        val toDisplayTitle = if (title != null) {
            if (hasNoScores || title.shouldRender) {
                replacePlaceholders(title.currentText ?: "", player)
            } else null
        } else ""

        val toDisplayScores = HashMap<Int, String?>()
        scores.forEach { (score, value) ->
            if (value != null) {
                if (hasNoScores || value.shouldRender) {
                    toDisplayScores[score] = preventDuplicates(
                        replacePlaceholders(
                            value.currentText ?: "", player
                        ), toDisplayScores.values
                    )
                } else toDisplayScores[score] = null
            }
        }

        return (toDisplayTitle to toDisplayScores)
    }

    private fun replacePlaceholders(input: String, player: Player): String {
        val result = VariablesReplacer.replace(PlaceholderReplacer.replace(input, player), player)
        return translateHexColorCodes(translateAlternateColorCodes('&', result))
    }

    private fun preventDuplicates(text: String, values: Collection<String?>): String {
        return if (values.contains(text)) {
            preventDuplicates("${ChatColor.RESET}$text", values)
        } else text
    }
}