package com.r4g3baby.simplescore.scoreboard

import com.comphenix.protocol.ProtocolLibrary
import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.CompatibilityMode
import com.r4g3baby.simplescore.scoreboard.handlers.BukkitScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ProtocolScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ScoreboardHandler
import com.r4g3baby.simplescore.scoreboard.listeners.McMMOListener
import com.r4g3baby.simplescore.scoreboard.listeners.PacketListener
import com.r4g3baby.simplescore.scoreboard.listeners.PlayerListener
import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
import com.r4g3baby.simplescore.scoreboard.placeholders.PlaceholderProvider
import com.r4g3baby.simplescore.scoreboard.tasks.ScoreboardTask
import com.r4g3baby.simplescore.scoreboard.worldguard.WorldGuardAPI
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
        val forceLegacy = SimpleScore.config.forceLegacy
        val hasProtocolLib = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")
        scoreboardHandler = if (!forceLegacy && hasProtocolLib) ProtocolScoreboard() else BukkitScoreboard()

        Bukkit.getPluginManager().apply {
            registerEvents(PlayerListener(), SimpleScore.plugin)
            if (!hasProtocolLib && isPluginEnabled("mcMMO")) {
                registerEvents(McMMOListener(), SimpleScore.plugin)
            }
        }

        if (SimpleScore.config.compatibilityMode != CompatibilityMode.NONE) {
            if (hasProtocolLib) PacketListener().let { packetListener ->
                ProtocolLibrary.getProtocolManager().addPacketListener(packetListener)
                Bukkit.getPluginManager().registerEvents(packetListener, SimpleScore.plugin)
            }
        }

        if (!hasProtocolLib) with(SimpleScore.plugin.logger) {
            info("It looks like your server doesn't have ProtocolLib, install ProtocolLib for even better performance and compatibility with other plugins.")
            info("https://www.spigotmc.org/resources/protocollib.1997/")
        }

        PlaceholderProvider()

        ScoreboardTask().runTaskTimerAsynchronously(SimpleScore.plugin, 20L, 1L)
    }

    internal fun reload() {
        scoreboards.clearCache()

        playersData.forEach { (_, playerData) ->
            playerData.getScoreboard(SimpleScore.plugin)?.let { scoreboard ->
                playerData.setScoreboard(SimpleScore.plugin, scoreboards.get(scoreboard)?.name)
            }
        }

        Bukkit.getOnlinePlayers().forEach { scoreboardHandler.clearScoreboard(it) }
    }

    internal fun needsScoreboard(player: Player): Boolean {
        playersData.get(player).let { playerData ->
            val worldBoards = scoreboards.getForWorld(player.world).filter { it.canSee(player) }
            val regionBoards = WorldGuardAPI.getFlag(player, player.location)
            return player.isOnline && !playerData.isDisabled && (
                playerData.hasScoreboards || worldBoards.isNotEmpty() || regionBoards.isNotEmpty()
            )
        }
    }

    internal fun updateScoreboardState(player: Player, to: Location = player.location, from: Location? = null) {
        playersData.get(player).let { playerData ->
            val worldBoards = scoreboards.getForWorld(to.world).filter { it.canSee(player) }
            val regionBoards = WorldGuardAPI.getFlag(player, to)
            val needsScoreboard = player.isOnline && !playerData.isDisabled && (
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
            return SimpleScore.config.scoreboards[scoreboard]
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
            return SimpleScore.config.scoreboards.iterator()
        }
    }

    class PlayersData : Iterable<Map.Entry<UUID, PlayerData>> {
        private val playersData = HashMap<UUID, PlayerData>()

        internal fun loadPlayer(player: Player) = loadPlayer(player.uniqueId)
        internal fun loadPlayer(uniqueId: UUID): PlayerData {
            var playerData = SimpleScore.storage.fetchPlayer(uniqueId)
            if (playerData == null) {
                playerData = PlayerData()
                SimpleScore.storage.createPlayer(uniqueId, playerData)
            }
            playersData[uniqueId] = playerData
            return playerData
        }

        internal fun unloadPlayer(player: Player) = unloadPlayer(player.uniqueId)
        internal fun unloadPlayer(uniqueId: UUID) {
            playersData.remove(uniqueId)?.let { playerData ->
                SimpleScore.storage.savePlayer(uniqueId, playerData)
            }
        }

        fun get(player: Player) = get(player.uniqueId)
        fun get(uniqueId: UUID): PlayerData {
            return playersData.getOrDefault(uniqueId, PlayerData())
        }

        fun getScoreboards(player: Player): List<String> {
            return get(player).scoreboards
        }

        fun hasScoreboards(player: Player): Boolean {
            return get(player).hasScoreboards
        }

        fun getScoreboard(plugin: Plugin, player: Player): String? {
            return get(player).getScoreboard(plugin)
        }

        fun setScoreboard(plugin: Plugin, player: Player, scoreboard: String?) {
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