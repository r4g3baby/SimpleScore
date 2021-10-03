package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.plugin.Plugin
import java.util.function.Predicate
import java.util.regex.Pattern

class MainConfig(plugin: Plugin) : ConfigFile(plugin, "config") {
    val version = config.getInt("version", -1)
    val language = config.getString("language", "en")
    val checkForUpdates = config.getBoolean("checkForUpdates", true)
    val savePlayerData = config.getBoolean("savePlayerData", true)
    val asyncPlaceholders = config.getBoolean("asyncPlaceholders", true)
    val forceLegacy = config.getBoolean("forceLegacy", false)

    private val scoreboardsConfig = ScoreboardsConfig(plugin)
    val scoreboards get() = scoreboardsConfig.scoreboards
    val worlds = HashMap<Predicate<String>, List<String>>()

    init {
        if (config.isConfigurationSection("worlds")) {
            val worldsSec = config.getConfigurationSection("worlds")
            worldsSec.getKeys(false).forEach { world ->
                val pattern = Pattern.compile("^${world}$", Pattern.CASE_INSENSITIVE)
                worlds[pattern.asPredicate()] = when {
                    worldsSec.isList(world) -> mutableListOf<String>().also { list ->
                        worldsSec.getStringList(world).forEach { scoreboard ->
                            list.add(scoreboard)
                        }
                    }.toList()
                    worldsSec.isString(world) -> listOf(worldsSec.getString(world))
                    else -> emptyList()
                }
            }
        }
    }
}