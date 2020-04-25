package com.r4g3baby.simplescore.scoreboard

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.listeners.PlayersListener
import com.r4g3baby.simplescore.scoreboard.models.ScoreboardWorld
import com.r4g3baby.simplescore.scoreboard.tasks.ScoreboardRunnable
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import java.io.*
import java.util.*
import java.util.logging.Level
import kotlin.collections.HashSet

class ScoreboardManager(private val plugin: SimpleScore) {
    private val _disabledDataFile = File(plugin.dataFolder, "data${File.separator}scoreboards")
    private val disabledScoreboards = HashSet<UUID>()

    init {
        plugin.server.pluginManager.registerEvents(PlayersListener(plugin), plugin)

        if (plugin.config.saveScoreboards) {
            plugin.logger.info("Loading disabled scoreboards...")

            try {
                if (_disabledDataFile.exists()) {
                    FileInputStream(_disabledDataFile).use { fis ->
                        ObjectInputStream(fis).use { ois ->
                            val content = ois.readObject()
                            if (content is ArrayList<*>) {
                                disabledScoreboards.addAll(content.filterIsInstance<UUID>())
                            }
                        }
                    }
                }

                plugin.logger.info("Disabled scoreboards loaded.")
            } catch (e: IOException) {
                plugin.logger.log(Level.WARNING, "Error while loading disabled scoreboards", e)
            }
        }

        ScoreboardRunnable(plugin).runTaskTimer(plugin, 20L, 1L)
    }

    fun reload() {
        if (!plugin.config.saveScoreboards) {
            disabledScoreboards.clear()
        }
    }

    fun disable() {
        if (plugin.config.saveScoreboards) {
            plugin.logger.info("Saving disabled scoreboards...")

            try {
                if (!_disabledDataFile.parentFile.exists()) {
                    _disabledDataFile.parentFile.mkdirs()
                }
                if (!_disabledDataFile.exists()) {
                    _disabledDataFile.createNewFile()
                }

                FileOutputStream(_disabledDataFile).use { fos ->
                    ObjectOutputStream(fos).use { oos ->
                        oos.writeObject(disabledScoreboards)
                    }
                }

                plugin.logger.info("Disabled scoreboards saved.")
            } catch (e: IOException) {
                plugin.logger.log(Level.WARNING, "Error while saving disabled scoreboards", e)
            }
        }
    }

    fun toggleScoreboard(player: Player): Boolean {
        return if (disabledScoreboards.contains(player.uniqueId)) {
            disabledScoreboards.remove(player.uniqueId)
            createObjective(player)
            false
        } else {
            disabledScoreboards.add(player.uniqueId)
            removeObjective(player)
            true
        }
    }

    fun createObjective(player: Player) {
        if (!disabledScoreboards.contains(player.uniqueId)) {
            if (player.scoreboard != null && player.scoreboard != plugin.server.scoreboardManager.mainScoreboard) {
                player.scoreboard.getObjective(getPlayerIdentifier(player))?.unregister()
            } else {
                player.scoreboard = plugin.server.scoreboardManager.newScoreboard
            }
            val objective = player.scoreboard.registerNewObjective(getPlayerIdentifier(player), "dummy")
            objective.displaySlot = DisplaySlot.SIDEBAR
        }
    }

    fun removeObjective(player: Player) {
        getObjective(player)?.unregister()
    }

    fun hasObjective(player: Player): Boolean {
        return getObjective(player) != null
    }

    fun getObjective(player: Player): Objective? {
        return player.scoreboard?.getObjective(getPlayerIdentifier(player))
    }

    fun hasScoreboard(world: World): Boolean {
        return plugin.config.worlds.containsKey(world.name.toLowerCase())
    }

    fun getScoreboard(world: World): ScoreboardWorld? {
        return plugin.config.worlds[world.name.toLowerCase()]
    }

    private fun getPlayerIdentifier(player: Player): String {
        return "sb${player.uniqueId.toString().replace("-", "").substring(0..13)}"
    }
}