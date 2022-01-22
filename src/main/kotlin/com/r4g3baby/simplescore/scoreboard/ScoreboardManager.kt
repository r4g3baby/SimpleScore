package com.r4g3baby.simplescore.scoreboard

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.handlers.BukkitScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ProtocolScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ScoreboardHandler
import com.r4g3baby.simplescore.scoreboard.listeners.McMMOListener
import com.r4g3baby.simplescore.scoreboard.listeners.PlayersListener
import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
import com.r4g3baby.simplescore.scoreboard.placeholders.PlaceholderProvider
import com.r4g3baby.simplescore.scoreboard.tasks.ScoreboardTask
import com.r4g3baby.simplescore.scoreboard.worldguard.WorldGuardAPI
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import com.r4g3baby.simplescore.utils.isEqual
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class ScoreboardManager {
    internal val scoreboardHandler: ScoreboardHandler

    val scoreboards: Scoreboards = Scoreboards()
    val playersData: PlayersData = PlayersData()

    init {
        Bukkit.getPluginManager().apply {
            registerEvents(PlayersListener(), SimpleScore.plugin)
            if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
                registerEvents(McMMOListener(), SimpleScore.plugin)
            }
        }

        PlaceholderProvider()

        scoreboardHandler = if (!SimpleScore.config.forceLegacy) {
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
                ProtocolScoreboard()
            } else BukkitScoreboard()
        } else BukkitScoreboard()

        ScoreboardTask().runTaskTimerAsynchronously(SimpleScore.plugin, 20L, 1L)
    }

    internal fun reload() {
        scoreboards.clearCache()
        if (!SimpleScore.config.savePlayerData) {
            playersData.clearPlayerData()
        }

        playersData.forEach { (_, playerData) ->
            playerData.getScoreboard(SimpleScore.plugin)?.let { scoreboard ->
                playerData.setScoreboard(SimpleScore.plugin, scoreboards.get(scoreboard.name))
            }
        }

        Bukkit.getOnlinePlayers().forEach { scoreboardHandler.clearScoreboard(it) }
    }

    internal fun updateScoreboardState(player: Player, to: Location = player.location, from: Location? = null) {
        playersData.get(player).let { playerData ->
            val worldBoards = scoreboards.getForWorld(to.world).filter { it.canSee(player) }
            val regionBoards = WorldGuardAPI.getFlag(player, to)
            val needsScoreboard = !playerData.isDisabled && (
                playerData.hasScoreboards || worldBoards.isNotEmpty() || regionBoards.isNotEmpty()
            )

            if (scoreboardHandler.hasScoreboard(player)) {
                if (!needsScoreboard) scoreboardHandler.removeScoreboard(player)
                else if (playerData.isHidden) scoreboardHandler.clearScoreboard(player)
                else if (from != null) {
                    val fromWorldBoards = scoreboards.getForWorld(from.world).filter { it.canSee(player) }
                    val fromRegionBoards = WorldGuardAPI.getFlag(player, from)
                    if (!fromWorldBoards.isEqual(worldBoards) || !fromRegionBoards.isEqual(regionBoards)) {
                        Bukkit.getScheduler().runTask(SimpleScore.plugin) {
                            scoreboardHandler.clearScoreboard(player)
                        }
                    }
                }
            } else if (needsScoreboard) scoreboardHandler.createScoreboard(player)
        }
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
                            scoreboards.mapNotNull { get(it) }.forEach { scoreboard ->
                                list.add(scoreboard)
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

    class PlayersData : Iterable<Map.Entry<UUID, PlayerData>> {
        private val playersData = HashMap<UUID, PlayerData>()
        private val playersDataFile: ConfigFile by lazy {
            ConfigFile(SimpleScore.plugin, "playersData").apply {
                config.options().header("This file is generated by the plugin to store player information.")
            }
        }

        init {
            if (SimpleScore.config.savePlayerData) {
                SimpleScore.plugin.logger.info("Loading player data...")

                playersDataFile.config.apply {
                    getKeys(false).forEach { uniqueId ->
                        val playerSection = getConfigurationSection(uniqueId)
                        val isHidden = playerSection.getBoolean("isHidden", false)
                        val isDisabled = playerSection.getBoolean("isDisabled", false)
                        val scoreboard = playerSection.getString("scoreboard", null)?.let { scoreboard ->
                            SimpleScore.config.scoreboards[scoreboard.lowercase()]
                        }

                        playersData[UUID.fromString(uniqueId)] = PlayerData(
                            if (isHidden) mutableSetOf(SimpleScore.plugin) else mutableSetOf(),
                            if (isDisabled) mutableSetOf(SimpleScore.plugin) else mutableSetOf(),
                            if (scoreboard != null) LinkedHashMap(
                                mapOf(SimpleScore.plugin to scoreboard)
                            ) else LinkedHashMap()
                        )
                    }
                }

                SimpleScore.plugin.logger.info("Player data loaded.")
            }
        }

        fun save() {
            if (SimpleScore.config.savePlayerData) {
                SimpleScore.plugin.logger.info("Saving player data...")

                playersDataFile.config.apply {
                    // Clear current player data
                    getKeys(false).forEach { set(it, null) }

                    playersData.forEach { (uniqueId, playerData) ->
                        val isHidden = playerData.isHiding(SimpleScore.plugin)
                        val isDisabled = playerData.isDisabling(SimpleScore.plugin)
                        val scoreboard = playerData.getScoreboard(SimpleScore.plugin)

                        if (isHidden || isDisabled || scoreboard != null) {
                            createSection(
                                uniqueId.toString(), mapOf(
                                    "isHidden" to if (isHidden) true else null,
                                    "isDisabled" to if (isDisabled) true else null,
                                    "scoreboard" to scoreboard?.name
                                )
                            )
                        }
                    }

                    save(playersDataFile)
                }

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

        fun getScoreboards(player: Player): List<Scoreboard> {
            return get(player).scoreboards
        }

        fun hasScoreboards(player: Player): Boolean {
            return get(player).hasScoreboards
        }

        fun getScoreboard(plugin: Plugin, player: Player): Scoreboard? {
            return get(player).getScoreboard(plugin)
        }

        fun setScoreboard(plugin: Plugin, player: Player, scoreboard: Scoreboard?) {
            get(player).setScoreboard(plugin, scoreboard)
            SimpleScore.manager.updateScoreboardState(player)
        }

        fun isHidden(player: Player): Boolean {
            return get(player).isHidden
        }

        fun isHiding(plugin: Plugin, player: Player): Boolean {
            return get(player).isHiding(plugin)
        }

        fun setHidden(plugin: Plugin, player: Player, hidden: Boolean) {
            get(player).also { playerData ->
                if (hidden) playerData.hide(plugin) else playerData.show(plugin)
            }

            SimpleScore.manager.updateScoreboardState(player)
        }

        fun toggleHidden(plugin: Plugin, player: Player): Boolean {
            val isHidden = get(player).let { playerData ->
                if (playerData.hide(plugin)) return@let true
                return@let !playerData.show(plugin)
            }

            SimpleScore.manager.updateScoreboardState(player)
            return isHidden
        }

        fun isDisabled(player: Player): Boolean {
            return get(player).isDisabled
        }

        fun isDisabling(plugin: Plugin, player: Player): Boolean {
            return get(player).isDisabling(plugin)
        }

        fun setDisabled(plugin: Plugin, player: Player, disabled: Boolean) {
            get(player).also { playerData ->
                if (disabled) playerData.disable(plugin) else playerData.enable(plugin)
            }

            SimpleScore.manager.updateScoreboardState(player)
        }

        fun toggleDisabled(plugin: Plugin, player: Player): Boolean {
            val isDisabled = get(player).let { playerData ->
                if (playerData.disable(plugin)) return@let true
                return@let !playerData.enable(plugin)
            }

            SimpleScore.manager.updateScoreboardState(player)
            return isDisabled
        }

        override fun iterator(): Iterator<Map.Entry<UUID, PlayerData>> {
            return playersData.asIterable().iterator()
        }
    }
}