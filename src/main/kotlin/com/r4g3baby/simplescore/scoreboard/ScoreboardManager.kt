package com.r4g3baby.simplescore.scoreboard

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.handlers.BukkitScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ProtocolScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ScoreboardHandler
import com.r4g3baby.simplescore.scoreboard.listeners.PlayersListener
import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
import com.r4g3baby.simplescore.scoreboard.placeholders.ScoreboardExpansion
import com.r4g3baby.simplescore.scoreboard.tasks.ScoreboardTask
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class ScoreboardManager {
    private val scoreboardHandler: ScoreboardHandler

    val scoreboards: Scoreboards = Scoreboards()
    val playersData: PlayersData = PlayersData()

    init {
        Bukkit.getPluginManager().apply {
            registerEvents(PlayersListener(), SimpleScore.plugin)
            if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
                registerEvents(McMMOListener(), SimpleScore.plugin)
            }
        }

        if (SimpleScore.usePlaceholderAPI) {
            ScoreboardExpansion(SimpleScore.plugin).register()
        }

        scoreboardHandler = if (!SimpleScore.config.forceLegacy) {
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
                ProtocolScoreboard()
            } else BukkitScoreboard()
        } else BukkitScoreboard()

        ScoreboardTask().runTaskTimerAsynchronously(SimpleScore.plugin, 20L, 1L)
    }

    fun reload() {
        scoreboards.clearCache()
        if (!SimpleScore.config.savePlayerData) {
            playersData.clearPlayerData()
        }
        Bukkit.getOnlinePlayers().forEach { clearScoreboard(it) }
    }

    fun createScoreboard(player: Player) {
        if (!playersData.get(player).isDisabled) {
            scoreboardHandler.createScoreboard(player)
        }
    }

    fun removeScoreboard(player: Player) {
        scoreboardHandler.removeScoreboard(player)
    }

    fun hasScoreboard(player: Player): Boolean {
        return scoreboardHandler.hasScoreboard(player)
    }

    fun clearScoreboard(player: Player) {
        scoreboardHandler.clearScoreboard(player)
    }

    fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player) {
        val playerData = playersData.get(player)
        if (!playerData.isHidden && !playerData.isDisabled) {
            scoreboardHandler.updateScoreboard(title, scores, player)
        }
    }

    fun hasLineLengthLimit(): Boolean {
        return scoreboardHandler.hasLineLengthLimit()
    }

    class Scoreboards : Iterable<Map.Entry<String, Scoreboard>> {
        private val worldScoreboardsCache = HashMap<String, List<Scoreboard>>()

        fun get(scoreboard: String): Scoreboard? {
            return SimpleScore.config.scoreboards[scoreboard.lowercase()]
        }

        fun getForWorld(world: World) = getForWorld(world.name)
        fun getForWorld(world: String): List<Scoreboard> {
            return worldScoreboardsCache.computeIfAbsent(world) {
                mutableListOf<Scoreboard>().also { list ->
                    SimpleScore.config.worlds.forEach { (predicate, scoreboards) ->
                        if (predicate.test(world)) {
                            scoreboards.forEach {
                                val scoreboard = SimpleScore.config.scoreboards[it]
                                if (scoreboard != null) {
                                    list.add(scoreboard)
                                }
                            }
                            return@forEach
                        }
                    }
                }.toList()
            }
        }

        fun clearCache() {
            worldScoreboardsCache.clear()
        }

        override fun iterator(): Iterator<Map.Entry<String, Scoreboard>> {
            return SimpleScore.config.scoreboards.asIterable().iterator()
        }
    }

    class PlayersData {
        private val playersData = HashMap<UUID, PlayerData>()

        init {
            if (SimpleScore.config.savePlayerData) {
                SimpleScore.plugin.logger.info("Loading player data...")

                // todo: load player data

                SimpleScore.plugin.logger.info("Player data loaded.")
            }
        }

        fun save() {
            if (SimpleScore.config.savePlayerData) {
                SimpleScore.plugin.logger.info("Saving player data...")

                // todo: save player data

                SimpleScore.plugin.logger.info("Player data saved.")
            }
        }

        fun clearPlayerData() {
            playersData.clear()
        }

        fun get(player: Player) = get(player.uniqueId)
        fun get(uniqueId: UUID): PlayerData {
            return playersData.getOrPut(uniqueId) {
                return@getOrPut PlayerData()
            }
        }

        fun toggleForceHidden(player: Player): Boolean {
            return get(player).let { playerData ->
                playerData.isForceHidden = !playerData.isForceHidden
                if (playerData.isForceHidden) SimpleScore.scoreboardManager.clearScoreboard(player)
                return@let playerData.isForceHidden
            }
        }

        fun toggleForceDisabled(player: Player): Boolean {
            return get(player).let { playerData ->
                playerData.isForceDisabled = !playerData.isForceDisabled
                if (playerData.isForceDisabled) {
                    SimpleScore.scoreboardManager.removeScoreboard(player)
                } else SimpleScore.scoreboardManager.createScoreboard(player)
                return@let playerData.isForceDisabled
            }
        }

        fun toggleHidden(plugin: Plugin, player: Player): Boolean {
            return get(player).let { playerData ->
                if (!playerData.show(plugin)) {
                    val wasHiddenBefore = playerData.isHidden
                    playerData.hide(plugin)
                    if (!wasHiddenBefore) SimpleScore.scoreboardManager.clearScoreboard(player)
                    return@let false
                }
                return@let true
            }
        }

        fun toggleDisabled(plugin: Plugin, player: Player): Boolean {
            return get(player).let { playerData ->
                val wasDisabledBefore = playerData.isDisabled
                if (!playerData.enable(plugin)) {
                    playerData.disable(plugin)
                    if (!wasDisabledBefore) SimpleScore.scoreboardManager.removeScoreboard(player)
                    return@let false
                }
                if (wasDisabledBefore) SimpleScore.scoreboardManager.createScoreboard(player)
                return@let true
            }
        }
    }

    @Deprecated("Use new playersData class", ReplaceWith("playersData.toggleForceHidden(player)"))
    fun toggleScoreboard(player: Player): Boolean {
        return playersData.toggleForceHidden(player)
    }

    @Deprecated("Use new playersData class", ReplaceWith("playersData.get(player).isHidden"))
    fun isScoreboardDisabled(player: Player): Boolean {
        return playersData.get(player).isHidden
    }

    @Deprecated("Use new scoreboards class", ReplaceWith("scoreboards.getForWorld(world)"))
    fun getWorldScoreboards(world: World): List<Scoreboard> {
        return scoreboards.getForWorld(world)
    }

    @Deprecated("Use new scoreboards class", ReplaceWith("scoreboards.get(scoreboard)"))
    fun getScoreboard(scoreboard: String): Scoreboard? {
        return scoreboards.get(scoreboard)
    }

    @Deprecated("Use new scoreboards class", ReplaceWith("scoreboards.map { it.value }"))
    fun getScoreboards(): List<Scoreboard> {
        return scoreboards.map { it.value }
    }
}