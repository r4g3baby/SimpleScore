package com.r4g3baby.simplescore.scoreboard

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.handlers.BukkitScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ProtocolScoreboard
import com.r4g3baby.simplescore.scoreboard.handlers.ScoreboardHandler
import com.r4g3baby.simplescore.scoreboard.listeners.PlayersListener
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
import com.r4g3baby.simplescore.scoreboard.placeholders.ScoreboardExpansion
import com.r4g3baby.simplescore.scoreboard.tasks.ScoreboardTask
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import java.io.*
import java.util.*
import java.util.logging.Level

class ScoreboardManager {
    private val _disabledDataFile = File(SimpleScore.plugin.dataFolder, "data${File.separator}scoreboards")

    private val worldScoreboardsCache = HashMap<World, List<Scoreboard>>()
    private val disabledScoreboards = HashSet<UUID>()
    private val scoreboardHandler: ScoreboardHandler

    init {
        Bukkit.getPluginManager().registerEvents(PlayersListener(), SimpleScore.plugin)

        if (SimpleScore.config.saveScoreboards) {
            SimpleScore.plugin.logger.info("Loading disabled scoreboards...")

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

                SimpleScore.plugin.logger.info("Disabled scoreboards loaded.")
            } catch (ex: IOException) {
                SimpleScore.plugin.logger.log(Level.WARNING, "Error while loading disabled scoreboards", ex)
            }
        }

        if (SimpleScore.usePlaceholderAPI) {
            ScoreboardExpansion(SimpleScore.plugin).register()
        }

        scoreboardHandler = if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            BukkitScoreboard()
        } else ProtocolScoreboard()

        ScoreboardTask().runTaskTimerAsynchronously(SimpleScore.plugin, 20L, 1L)
    }

    fun reload() {
        worldScoreboardsCache.clear()
        if (!SimpleScore.config.saveScoreboards) {
            disabledScoreboards.clear()
        }
        Bukkit.getOnlinePlayers().forEach { clearScoreboard(it) }
    }

    fun disable() {
        if (SimpleScore.config.saveScoreboards) {
            SimpleScore.plugin.logger.info("Saving disabled scoreboards...")

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

                SimpleScore.plugin.logger.info("Disabled scoreboards saved.")
            } catch (ex: IOException) {
                SimpleScore.plugin.logger.log(Level.WARNING, "Error while saving disabled scoreboards", ex)
            }
        }
    }

    fun createScoreboard(player: Player) {
        scoreboardHandler.createScoreboard(player)
    }

    fun removeScoreboard(player: Player) {
        scoreboardHandler.removeScoreboard(player)
    }

    fun clearScoreboard(player: Player) {
        scoreboardHandler.clearScoreboard(player)
    }

    fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player) {
        if (!disabledScoreboards.contains(player.uniqueId)) {
            scoreboardHandler.updateScoreboard(title, scores, player)
        }
    }

    fun hasScoreboard(player: Player): Boolean {
        return scoreboardHandler.hasScoreboard(player)
    }

    fun hasLineLengthLimit(): Boolean {
        return scoreboardHandler.hasLineLengthLimit()
    }

    fun toggleScoreboard(player: Player): Boolean {
        return if (!disabledScoreboards.remove(player.uniqueId)) {
            disabledScoreboards.add(player.uniqueId)
            scoreboardHandler.clearScoreboard(player)
            true
        } else false
    }

    fun isScoreboardDisabled(player: Player): Boolean {
        return disabledScoreboards.contains(player.uniqueId)
    }

    fun getWorldScoreboards(world: World): List<Scoreboard> {
        return worldScoreboardsCache.computeIfAbsent(world) {
            mutableListOf<Scoreboard>().also { list ->
                SimpleScore.config.worlds.forEach { (predicate, scoreboards) ->
                    if (predicate.test(world.name)) {
                        scoreboards.forEach {
                            val scoreboard = SimpleScore.config.scoreboards[it]
                            if (scoreboard != null) {
                                list.add(scoreboard)
                            }
                        }
                    }
                }
            }.toList()
        }
    }

    fun getScoreboard(scoreboard: String): Scoreboard? {
        return SimpleScore.config.scoreboards[scoreboard.toLowerCase()]
    }

    fun getScoreboards(): List<Scoreboard> {
        return SimpleScore.config.scoreboards.values.toList()
    }
}