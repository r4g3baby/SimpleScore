package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import java.util.function.Predicate
import java.util.regex.Pattern

class MainConfig(plugin: SimpleScore) : ConfigFile(plugin, "config") {
    private val version = config.getInt("version", -1)
    private var updateTime = config.getInt("updateTime", 20)
    var saveScoreboards = config.getBoolean("saveScoreboards", true)
        private set
    var asyncPlaceholders = config.getBoolean("asyncPlaceholders", true)
        private set
    var forceLegacy = config.getBoolean("forceLegacy", false)
        private set

    private var scoreboardsConfig = ScoreboardsConfig(plugin, updateTime)
    val scoreboards get() = scoreboardsConfig.scoreboards
    val worlds = HashMap<Predicate<String>, List<String>>()

    init {
        if (version < 0) {
            if (config.contains("UpdateTime")) {
                updateTime = config.getInt("UpdateTime", updateTime)
                config.set("updateTime", updateTime)
                config.set("UpdateTime", null)
            }

            if (config.contains("SaveScoreboards")) {
                saveScoreboards = config.getBoolean("SaveScoreboards", saveScoreboards)
                config.set("saveScoreboards", saveScoreboards)
                config.set("SaveScoreboards", null)
            }

            if (config.contains("AsyncPlaceholders")) {
                asyncPlaceholders = config.getBoolean("AsyncPlaceholders", asyncPlaceholders)
                config.set("asyncPlaceholders", asyncPlaceholders)
                config.set("AsyncPlaceholders", null)
            }

            if (config.contains("ForceLegacy")) {
                forceLegacy = config.getBoolean("ForceLegacy", forceLegacy)
                config.set("forceLegacy", forceLegacy)
                config.set("ForceLegacy", null)
            }

            if (config.isConfigurationSection("Scoreboards")) {
                scoreboardsConfig.saveOldScoreboards(config.getConfigurationSection("Scoreboards"))
                scoreboardsConfig = ScoreboardsConfig(plugin, updateTime)
                config.set("Scoreboards", null)
            }

            if (config.isConfigurationSection("Worlds")) {
                config.set("worlds", config.getConfigurationSection("Worlds"))
                config.set("Worlds", null)
            }

            config.set("version", 1)
            config.save(this)
        }

        if (config.isConfigurationSection("worlds")) {
            val worldsSec = config.getConfigurationSection("worlds")
            for (world in worldsSec.getKeys(false)) {
                val pattern = Pattern.compile("^${world}$", Pattern.CASE_INSENSITIVE)
                val scoreboards = worldsSec.get(world)
                worlds[pattern.asPredicate()] = when (scoreboards) {
                    is List<*> -> mutableListOf<String>().also { list ->
                        scoreboards.forEach {
                            if (it is String) {
                                list.add(it.lowercase())
                            }
                        }
                    }.toList()
                    is String -> listOf(scoreboards.lowercase())
                    else -> emptyList()
                }
            }
        }
    }
}