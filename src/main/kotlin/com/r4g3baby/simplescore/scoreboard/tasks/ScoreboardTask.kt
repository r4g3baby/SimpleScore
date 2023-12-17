package com.r4g3baby.simplescore.scoreboard.tasks

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.handlers.BukkitScoreboard
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
import java.util.*

class ScoreboardTask : BukkitRunnable() {
    override fun run() {
        SimpleScore.manager.playersData.forEach { (_, playerData) -> playerData.scoreboard?.tick() }

        val possibleScoreboards = HashMap<Player, List<Scoreboard>>()
        for (world in Bukkit.getWorlds()) {
            val players = world.players.filter { player ->
                // Skip Citizens NPCs
                if (player.hasMetadata("NPC")) return@filter false

                val playerData = SimpleScore.manager.playersData.get(player)?.also { playerData ->
                    // No need to waste time computing scoreboards for players that won't see it
                    if (playerData.isHidden || playerData.isDisabled) return@filter false

                    // Set the list of possible scoreboards for this player
                    possibleScoreboards[player] = playerData.scoreboards.mapNotNull {
                        SimpleScore.manager.scoreboards.get(it)
                    }

                    // Player scoreboards override world and region scoreboards
                    return@filter !playerData.hasScoreboards
                }

                // Skip player if there is no playerData associated with it
                return@filter playerData != null
            }.toMutableList()
            if (players.size == 0) continue

            if (WorldGuardAPI.isEnabled) {
                val iterator = players.iterator()
                iterator.forEach { player ->
                    val regionBoards = WorldGuardAPI.getFlag(player)
                    if (regionBoards.isNotEmpty()) {
                        // Set the list of possible scoreboards for this player
                        possibleScoreboards[player] = regionBoards.mapNotNull {
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
                possibleScoreboards[player] = worldBoards
                iterator.remove()
            }
        }

        if (SimpleScore.config.asyncPlaceholders && SimpleScore.manager.scoreboardHandler !is BukkitScoreboard) {
            val playerScoreboards = getPlayerScoreboards(possibleScoreboards)
            Bukkit.getScheduler().runTask(SimpleScore.plugin) {
                updateScoreboards(playerScoreboards)
            }
        } else {
            Bukkit.getScheduler().runTask(SimpleScore.plugin) {
                updateScoreboards(getPlayerScoreboards(possibleScoreboards))
            }
        }
    }

    private fun updateScoreboards(playerBoards: Map<UUID, Pair<String?, Map<Int, String?>>>) {
        Bukkit.getOnlinePlayers().forEach { player ->
            with(SimpleScore.manager.scoreboardHandler) {
                val playerBoard = playerBoards[player.uniqueId]
                if (playerBoard != null) {
                    SimpleScore.manager.playersData.get(player)?.let { playerData ->
                        // Check if our player didn't hide/disable the scoreboard
                        if (!playerData.isHidden && !playerData.isDisabled) {
                            updateScoreboard(playerBoard.first, playerBoard.second, player)
                        }
                    }
                } else clearScoreboard(player)
            }
        }
    }

    private fun getPlayerScoreboards(possibleBoards: Map<Player, List<Scoreboard>>): Map<UUID, Pair<String?, Map<Int, String?>>> {
        return HashMap<UUID, Pair<String?, Map<Int, String?>>>().also { playerBoards ->
            possibleBoards.forEach { (player, scoreboards) ->
                for (scoreboard in scoreboards) {
                    if (scoreboard.canSee(player)) {
                        playerBoards[player.uniqueId] = getPlayerScoreboard(scoreboard, player)
                        break
                    }
                }
            }
        }
    }

    private fun getPlayerScoreboard(scoreboard: Scoreboard, player: Player): Pair<String?, Map<Int, String?>> {
        val playerScoreboard = SimpleScore.manager.playersData.get(player)?.let { playerData ->
            if (playerData.scoreboard == null || playerData.scoreboard?.name != scoreboard.name) {
                playerData.scoreboard = scoreboard.asPlayerScoreboard()
            }
            return@let playerData.scoreboard
        } ?: return "" to emptyMap() // this should never happen?

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