package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import java.util.function.Predicate
import java.util.regex.Pattern

class MainConfig(plugin: SimpleScore) : ConfigFile(plugin, "config") {
    val version = config.getInt("version", -1)
    val updateTime = config.getInt("updateTime", 20)
    val saveScoreboards = config.getBoolean("saveScoreboards", true)
    val asyncPlaceholders = config.getBoolean("asyncPlaceholders", true)
    val forceLegacy = config.getBoolean("forceLegacy", false)

    val scoreboardsConfig = ScoreboardsConfig(plugin, updateTime)
    val scoreboards get() = scoreboardsConfig.scoreboards
    val worlds = HashMap<Predicate<String>, List<String>>()

    init {
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