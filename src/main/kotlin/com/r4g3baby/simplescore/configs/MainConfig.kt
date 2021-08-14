package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.Scoreboard
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import java.util.function.Predicate
import java.util.regex.Pattern

class MainConfig(plugin: SimpleScore, fileName: String = "config") : ConfigFile(plugin, fileName) {
    private val version = config.getInt("version", -1)
    private val updateTime = config.getInt("updateTime", 20)
    val saveScoreboards = config.getBoolean("saveScoreboards", true)
    val asyncPlaceholders = config.getBoolean("asyncPlaceholders", true)
    val forceLegacy = config.getBoolean("forceLegacy", false)

    val scoreboards = HashMap<String, Scoreboard>()
    val worlds = HashMap<Predicate<String>, List<String>>()

    init {
        if (version < 0) {
            // Compatibility with older config format
            if (config.isConfigurationSection("Worlds") && !config.isConfigurationSection("Scoreboards")) {
                config.createSection("Scoreboards", config.getConfigurationSection("Worlds").getValues(true))
                config.set("Worlds", null)
            }

            if (config.isConfigurationSection("Shared") && !config.isConfigurationSection("Worlds")) {
                val sharedSec = config.getConfigurationSection("Shared")
                val worldsSec = config.createSection("Worlds")

                if (config.isConfigurationSection("Scoreboards")) {
                    for (scoreboard in config.getConfigurationSection("Scoreboards").getKeys(false)) {
                        worldsSec.set(scoreboard, scoreboard)
                    }
                }

                for (shared in sharedSec.getKeys(false)) {
                    for (world in sharedSec.getStringList(shared)) {
                        worldsSec.set(world, shared)
                    }
                }

                config.set("Shared", null)
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