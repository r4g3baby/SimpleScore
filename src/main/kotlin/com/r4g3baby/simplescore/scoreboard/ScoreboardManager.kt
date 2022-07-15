package com.r4g3baby.simplescore.scoreboard

import com.comphenix.protocol.ProtocolLibrary
import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.handlers.BukkitScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ProtocolScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ScoreboardHandler
import com.r4g3baby.simplescore.scoreboard.listeners.McMMOListener
import com.r4g3baby.simplescore.scoreboard.listeners.PacketListener
import com.r4g3baby.simplescore.scoreboard.listeners.PlayerListener
import com.r4g3baby.simplescore.scoreboard.models.CompatibilityMode
import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
import com.r4g3baby.simplescore.scoreboard.placeholders.PlaceholderProvider
import com.r4g3baby.simplescore.scoreboard.tasks.ScoreboardTask
import com.r4g3baby.simplescore.scoreboard.worldguard.WorldGuardAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

        Bukkit.getOnlinePlayers().forEach { player ->
            playersData.get(player)?.let { playerData ->
                playerData.getScoreboard(SimpleScore.plugin)?.let { scoreboard ->
                    playerData.setScoreboard(SimpleScore.plugin, scoreboards.get(scoreboard)?.name)
                }
                playerData.scoreboard = null
            }

            updateScoreboardState(player)
        }
    }

    internal fun needsScoreboard(player: Player, location: Location = player.location): Boolean {
        playersData.get(player)?.let { playerData ->
            val hasWorldBoard = scoreboards.getForWorld(location.world)
            val hasRegionBoard = WorldGuardAPI.getFlag(player, location)
            return player.isOnline && !playerData.isDisabled && (
                playerData.hasScoreboards || hasWorldBoard.isNotEmpty() || hasRegionBoard.isNotEmpty()
            )
        }
        return false
    }

    internal fun updateScoreboardState(player: Player, to: Location = player.location) {
        if (scoreboardHandler.hasScoreboard(player)) {
            if (!needsScoreboard(player, to)) scoreboardHandler.removeScoreboard(player)
        } else if (needsScoreboard(player, to)) scoreboardHandler.createScoreboard(player)
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
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
                }
            }
        }

        internal fun clearCache() {
            worldScoreboardsCache.clear()
        }

        override fun iterator(): Iterator<Map.Entry<String, Scoreboard>> {
            return SimpleScore.config.scoreboards.iterator()
        }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    class PlayersData : Iterable<Map.Entry<UUID, PlayerData>> {
        private val playersData = ConcurrentHashMap<UUID, PlayerData>()

        internal fun loadPlayer(player: Player) = loadPlayer(player.uniqueId)
        internal fun loadPlayer(uniqueId: UUID): PlayerData {
            var playerData = SimpleScore.storage.fetchPlayer(uniqueId)
            if (playerData == null) {
                playerData = PlayerData(uniqueId)
                SimpleScore.storage.createPlayer(playerData)
            }
            playersData[uniqueId] = playerData
            return playerData
        }

        internal fun unloadPlayer(player: Player) = unloadPlayer(player.uniqueId)
        internal fun unloadPlayer(uniqueId: UUID) {
            playersData.remove(uniqueId)?.let { playerData ->
                SimpleScore.storage.savePlayer(playerData)
            }
        }

        fun get(player: Player) = get(player.uniqueId)
        fun get(uniqueId: UUID): PlayerData? {
            return playersData[uniqueId]
        }

        fun getScoreboards(player: Player) = getScoreboards(player.uniqueId)
        fun getScoreboards(uniqueId: UUID) = get(uniqueId)?.let { getScoreboards(it) } ?: emptyList()
        fun getScoreboards(playerData: PlayerData): List<String> {
            return playerData.scoreboards
        }

        fun hasScoreboards(player: Player) = hasScoreboards(player.uniqueId)
        fun hasScoreboards(uniqueId: UUID) = get(uniqueId)?.let { hasScoreboards(it) } ?: false
        fun hasScoreboards(playerData: PlayerData): Boolean {
            return playerData.hasScoreboards
        }

        fun getScoreboard(plugin: Plugin, player: Player) = getScoreboard(plugin, player.uniqueId)
        fun getScoreboard(plugin: Plugin, uniqueId: UUID) = get(uniqueId)?.let { getScoreboard(plugin, it) }
        fun getScoreboard(plugin: Plugin, playerData: PlayerData): String? {
            return playerData.getScoreboard(plugin)
        }

        fun setScoreboard(plugin: Plugin, player: Player, scoreboard: String?) = setScoreboard(plugin, player.uniqueId, scoreboard)
        fun setScoreboard(plugin: Plugin, uniqueId: UUID, scoreboard: String?) = get(uniqueId)?.let { setScoreboard(plugin, it, scoreboard) }
        fun setScoreboard(plugin: Plugin, playerData: PlayerData, scoreboard: String?) {
            playerData.setScoreboard(plugin, scoreboard)
            Bukkit.getPlayer(playerData.uniqueId)?.let { player ->
                SimpleScore.manager.updateScoreboardState(player)
            }
        }

        fun isHidden(player: Player) = isHidden(player.uniqueId)
        fun isHidden(uniqueId: UUID) = get(uniqueId)?.let { isHidden(it) } ?: false
        fun isHidden(playerData: PlayerData): Boolean {
            return playerData.isHidden
        }

        fun isHiding(plugin: Plugin, player: Player) = isHiding(plugin, player.uniqueId)
        fun isHiding(plugin: Plugin, uniqueId: UUID) = get(uniqueId)?.let { isHiding(plugin, it) } ?: false
        fun isHiding(plugin: Plugin, playerData: PlayerData): Boolean {
            return playerData.isHiding(plugin)
        }

        fun setHidden(plugin: Plugin, player: Player, hidden: Boolean) = setHidden(plugin, player.uniqueId, hidden)
        fun setHidden(plugin: Plugin, uniqueId: UUID, hidden: Boolean) = get(uniqueId)?.let { setHidden(plugin, it, hidden) }
        fun setHidden(plugin: Plugin, playerData: PlayerData, hidden: Boolean) {
            if (hidden) playerData.hide(plugin) else playerData.show(plugin)
            Bukkit.getPlayer(playerData.uniqueId)?.let { player ->
                SimpleScore.manager.updateScoreboardState(player)
            }
        }

        fun toggleHidden(plugin: Plugin, player: Player) = toggleHidden(plugin, player.uniqueId)
        fun toggleHidden(plugin: Plugin, uniqueId: UUID) = get(uniqueId)?.let { toggleHidden(plugin, it) } ?: false
        fun toggleHidden(plugin: Plugin, playerData: PlayerData): Boolean {
            val isHidden = run {
                if (playerData.hide(plugin)) return@run true
                return@run !playerData.show(plugin)
            }

            Bukkit.getPlayer(playerData.uniqueId)?.let { player ->
                SimpleScore.manager.updateScoreboardState(player)
            }

            return isHidden
        }

        fun isDisabled(player: Player) = isDisabled(player.uniqueId)
        fun isDisabled(uniqueId: UUID) = get(uniqueId)?.let { isDisabled(it) } ?: false
        fun isDisabled(playerData: PlayerData): Boolean {
            return playerData.isDisabled
        }

        fun isDisabling(plugin: Plugin, player: Player) = isDisabling(plugin, player.uniqueId)
        fun isDisabling(plugin: Plugin, uniqueId: UUID) = get(uniqueId)?.let { isDisabling(plugin, it) } ?: false
        fun isDisabling(plugin: Plugin, playerData: PlayerData): Boolean {
            return playerData.isDisabling(plugin)
        }

        fun setDisabled(plugin: Plugin, player: Player, disabled: Boolean) = setDisabled(plugin, player.uniqueId, disabled)
        fun setDisabled(plugin: Plugin, uniqueId: UUID, disabled: Boolean) = get(uniqueId)?.let { setDisabled(plugin, it, disabled) }
        fun setDisabled(plugin: Plugin, playerData: PlayerData, disabled: Boolean) {
            if (disabled) playerData.disable(plugin) else playerData.enable(plugin)
            Bukkit.getPlayer(playerData.uniqueId)?.let { player ->
                SimpleScore.manager.updateScoreboardState(player)
            }
        }

        fun toggleDisabled(plugin: Plugin, player: Player) = toggleDisabled(plugin, player.uniqueId)
        fun toggleDisabled(plugin: Plugin, uniqueId: UUID) = get(uniqueId)?.let { toggleDisabled(plugin, it) } ?: false
        fun toggleDisabled(plugin: Plugin, playerData: PlayerData): Boolean {
            val isDisabled = run {
                if (playerData.disable(plugin)) return@run true
                return@run !playerData.enable(plugin)
            }

            Bukkit.getPlayer(playerData.uniqueId)?.let { player ->
                SimpleScore.manager.updateScoreboardState(player)
            }

            return isDisabled
        }

        override fun iterator(): Iterator<Map.Entry<UUID, PlayerData>> {
            return playersData.iterator()
        }
    }
}