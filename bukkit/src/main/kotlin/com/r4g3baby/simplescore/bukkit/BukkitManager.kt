package com.r4g3baby.simplescore.bukkit

import com.r4g3baby.simplescore.BukkitPlugin
import com.r4g3baby.simplescore.api.scoreboard.Scoreboard
import com.r4g3baby.simplescore.api.scoreboard.data.Priority
import com.r4g3baby.simplescore.bukkit.command.MainCmd
import com.r4g3baby.simplescore.bukkit.config.MainConfig
import com.r4g3baby.simplescore.bukkit.hooks.PapiExpansion
import com.r4g3baby.simplescore.bukkit.listener.LuckPermsListener
import com.r4g3baby.simplescore.bukkit.listener.PlayerListener
import com.r4g3baby.simplescore.bukkit.protocol.legacy.LegacyProtocolHandler
import com.r4g3baby.simplescore.bukkit.protocol.modern.ModernProtocolHandler
import com.r4g3baby.simplescore.bukkit.protocol.modern.TeamsProtocolHandler
import com.r4g3baby.simplescore.bukkit.scoreboard.ScoreboardTask
import com.r4g3baby.simplescore.bukkit.scoreboard.VarReplacer
import com.r4g3baby.simplescore.bukkit.scoreboard.data.Viewer
import com.r4g3baby.simplescore.bukkit.util.ServerVersion
import com.r4g3baby.simplescore.bukkit.worldguard.WorldGuardAPI
import com.r4g3baby.simplescore.core.BaseManager
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class BukkitManager(private val plugin: BukkitPlugin) : BaseManager<Player, YamlConfiguration>(plugin) {
    override val varReplacer = VarReplacer(plugin)

    private val protocolHandler = when {
        ServerVersion.isBellow(ServerVersion.aquaticUpdate) -> LegacyProtocolHandler()
        ServerVersion.isBellow(ServerVersion.trailsAndTailsUpdate.copy(build = 3)) -> TeamsProtocolHandler()
        else -> ModernProtocolHandler()
    }

    override fun onEnable() {
        super.onEnable()

        plugin.getCommand(plugin.name)?.executor = MainCmd(plugin)
        plugin.server.pluginManager.registerEvents(PlayerListener(this), plugin)

        if (plugin.server.pluginManager.getPlugin("LuckPerms") != null) {
            LuckPermsListener(plugin)
        }

        if (varReplacer.usePlaceholderAPI) {
            PapiExpansion(plugin).register()
        }

        if (config.scoreboardTaskAsync) plugin.scheduler.runTaskTimerAsync(
            20L, config.taskUpdateTime, ScoreboardTask(this, protocolHandler)
        ) else plugin.scheduler.runTaskTimer(
            20L, config.taskUpdateTime, ScoreboardTask(this, protocolHandler)
        )
    }

    override fun onDisable() {
        HandlerList.unregisterAll(plugin)

        plugin.server.onlinePlayers.forEach { player ->
            removeViewer(player.uniqueId)
        }

        super.onDisable()
    }

    override lateinit var config: MainConfig
        private set

    override fun loadConfiguration() {
        config = MainConfig(plugin)
        super.loadConfiguration()

        // Clear world scoreboard cache
        worldScoreboardsCache.clear()

        // Make sure there are no conflicting scoreboards
        config.scoreboards.keys.forEach(scoreboardsMap::remove)

        // Refresh player scoreboards
        plugin.server.onlinePlayers.forEach { player ->
            val viewer = getOrCreateViewer(player)

            viewer.getScoreboard(plugin.provider)?.let { forcedScoreboard ->
                val scoreboard = getScoreboard(forcedScoreboard.name)
                viewer.setScoreboard(scoreboard, plugin.provider, Priority.Highest)
            }

            updateViewerWorldScoreboard(viewer, player.world)
            updateViewerRegionScoreboard(viewer, player.location)
        }
    }

    private val scoreboardsMap: MutableMap<String, Scoreboard<Player>> = ConcurrentHashMap()
    private val viewersMap: MutableMap<UUID, Viewer> = ConcurrentHashMap()

    override val scoreboards: List<Scoreboard<Player>>
        get() = config.scoreboards.values + scoreboardsMap.values

    override fun getScoreboard(name: String): Scoreboard<Player>? {
        return config.scoreboards[name] ?: scoreboardsMap[name]
    }

    override fun addScoreboard(scoreboard: Scoreboard<Player>): Scoreboard<Player>? {
        return scoreboardsMap.put(scoreboard.name, scoreboard)
    }

    override fun removeScoreboard(scoreboard: Scoreboard<Player>): Scoreboard<Player>? {
        return scoreboardsMap.remove(scoreboard.name)
    }

    override val viewers: List<Viewer>
        get() = viewersMap.values.toList()

    override fun getViewer(uniqueID: UUID): Viewer? {
        return viewersMap[uniqueID]
    }

    internal fun getOrCreateViewer(player: Player): Viewer {
        return viewersMap.computeIfAbsent(player.uniqueId) {
            Viewer(player).also { _ ->
                /* todo: fetch viewer information from storage
                plugin.scheduler.runTaskAsync {}
                */
            }
        }
    }

    internal fun removeViewer(uniqueID: UUID): Viewer? {
        return viewersMap.remove(uniqueID)?.also { viewer ->
            viewer.reference.get()?.takeIf { it.isOnline }?.let(protocolHandler::removeObjective)
        }
    }

    private val worldProvider = plugin.provider.withContext("world")
    internal fun updateViewerWorldScoreboard(viewer: Viewer, world: World) {
        val player = viewer.reference.get() ?: return
        val scoreboards = getForWorld(world)
        if (scoreboards.isNotEmpty()) {
            val scoreboard = scoreboards.firstOrNull { it.canSee(player, varReplacer) }
            viewer.setScoreboard(scoreboard, worldProvider, Priority.Low)
        } else viewer.removeScoreboard(worldProvider)
    }

    private val regionProvider = plugin.provider.withContext("region")
    internal fun updateViewerRegionScoreboard(viewer: Viewer, location: Location) {
        val player = viewer.reference.get() ?: return
        val scoreboards = WorldGuardAPI.getScoreboardFlag(player, location)
        if (scoreboards.isNotEmpty()) {
            val scoreboard = scoreboards.mapNotNull { getScoreboard(it) }.firstOrNull { it.canSee(player, varReplacer) }
            viewer.setScoreboard(scoreboard, regionProvider, Priority.Normal)
        } else viewer.removeScoreboard(regionProvider)
    }

    private val worldScoreboardsCache = mutableMapOf<String, List<Scoreboard<Player>>>()
    private fun getForWorld(world: World): List<Scoreboard<Player>> {
        return worldScoreboardsCache.computeIfAbsent(world.name) {
            for ((predicate, scoreboards) in config.worlds) {
                if (predicate.test(world.name)) {
                    return@computeIfAbsent scoreboards.mapNotNull(::getScoreboard)
                }
            }
            emptyList()
        }
    }
}
