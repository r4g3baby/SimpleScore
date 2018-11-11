package com.r4g3baby.simplescore.scoreboard

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.listeners.PlayersListener
import com.r4g3baby.simplescore.scoreboard.models.ScoreboardWorld
import com.r4g3baby.simplescore.scoreboard.tasks.ScoreboardRunnable
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import java.io.*
import java.util.*
import java.util.logging.Level
import kotlin.collections.ArrayList


class ScoreboardManager(private val plugin: SimpleScore) {
    private val _disabledDataFile = File(plugin.dataFolder, "data" + File.separator + "scoreboards")
    private var disabledScoreboards: MutableList<UUID> = ArrayList()
    private val scoreboardRunnable: ScoreboardRunnable
    private var scoreboardTask: BukkitTask

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
                                disabledScoreboards = content.filterIsInstance<UUID>() as MutableList<UUID>
                            }
                        }
                    }
                }

                plugin.logger.info("Disabled scoreboards loaded.")
            } catch (e: IOException) {
                plugin.logger.log(Level.WARNING, "Error while loading disabled scoreboards", e)
            }
        }

        scoreboardRunnable = ScoreboardRunnable(plugin)
        scoreboardTask = plugin.server.scheduler.runTaskTimerAsynchronously(plugin, scoreboardRunnable, 20L, plugin.config.updateTime)
    }

    fun reload() {
        if (!plugin.config.saveScoreboards) {
            disabledScoreboards.clear()
        }

        scoreboardTask.cancel()
        scoreboardTask = plugin.server.scheduler.runTaskTimerAsynchronously(plugin, scoreboardRunnable, 20L, plugin.config.updateTime)
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
            player.scoreboard = plugin.server.scoreboardManager.newScoreboard
            val objective = player.scoreboard.registerNewObjective(getPlayerIdentifier(player), "dummy")
            objective.displaySlot = DisplaySlot.SIDEBAR
        }
    }

    fun removeObjective(player: Player) {
        getObjective(player)?.unregister()
    }

    fun hasObjective(player: Player): Boolean {
        return player.scoreboard != null && player.scoreboard.getObjective(getPlayerIdentifier(player)) != null
    }

    fun getObjective(player: Player): Objective? {
        if (hasObjective(player)) {
            return player.scoreboard.getObjective(getPlayerIdentifier(player))
        }
        return null
    }

    fun hasScoreboard(world: World): Boolean {
        if (plugin.config.worlds.containsKey(world.name) || plugin.config.shared.any { it.value.contains(world.name) }) {
            return true
        }
        return false
    }

    fun getScoreboard(world: World): ScoreboardWorld? {
        val worlds = plugin.config.worlds
        return if (!worlds.containsKey(world.name)) {
            val shared = plugin.config.shared.filter { it.value.contains(world.name) }
            if (!shared.isEmpty()) {
                worlds[shared.keys.first()]
            } else {
                null
            }
        } else {
            worlds[world.name]
        }
    }

    private fun getPlayerIdentifier(player: Player): String {
        return player.uniqueId.toString().substring(0, 16)
    }
}