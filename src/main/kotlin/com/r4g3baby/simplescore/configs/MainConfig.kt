package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.configs.models.Storage
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.plugin.Plugin
import java.util.function.Predicate
import java.util.regex.Pattern

class MainConfig(plugin: Plugin) : ConfigFile(plugin, "config") {
    val version = config.getInt("version", -1)
    val language = config.getString("language", "en")
    val checkForUpdates = config.getBoolean("checkForUpdates", true)
    val asyncPlaceholders = config.getBoolean("asyncPlaceholders", true)
    val ignoreViaBackwards = config.getBoolean("ignoreViaBackwards", false)
    val forceMultiVersion = config.getBoolean("forceMultiVersion", false)
    val forceLegacy = config.getBoolean("forceLegacy", false)
    val storage = Storage(
        config.getString("storage.driver", "h2"),
        config.getString("storage.tablePrefix", "simplescore_"),
        config.getString("storage.address", "127.0.0.1:3306"),
        config.getString("storage.database", "minecraft"),
        config.getString("storage.username", "simplescore"),
        config.getString("storage.password", "|D/-\\55\\^/0|2|)"),
        Storage.Pool(
            config.getInt("storage.pool.maximumPoolSize", 8),
            config.getInt("storage.pool.minimumIdle", 8),
            config.getLong("storage.pool.maxLifetime", 1800000),
            config.getLong("storage.pool.keepaliveTime", 0),
            config.getLong("storage.pool.connectionTimeout", 5000),
            config.getConfigurationSection("storage.pool.extraProperties")?.getValues(false) ?: emptyMap()
        )
    )

    private val scoreboardsConfig = ScoreboardsConfig(plugin)
    val scoreboards get() = scoreboardsConfig.scoreboards
    val worlds = LinkedHashMap<Predicate<String>, List<String>>()

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