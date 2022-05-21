package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.scoreboard.models.CompatibilityMode
import com.r4g3baby.simplescore.storage.models.Storage
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import java.util.function.Predicate
import java.util.regex.Pattern

class MainConfig(plugin: Plugin) : ConfigFile(plugin, "config") {
    val version = config.getInt("version", -1)
    val language = config.getString("language", "en")
    val checkForUpdates = config.getBoolean("checkForUpdates", true)
    val asyncPlaceholders = config.getBoolean("asyncPlaceholders", true)
    val compatibilityMode = config.getCompatibilityMode("compatibilityMode")
    val forceMultiVersion = config.getBoolean("forceMultiVersion", false)
    val forceLegacy = config.getBoolean("forceLegacy", false)
    val storage = config.getStorage("storage")

    val conditions = ConditionsConfig(plugin)
    val scoreboards = ScoreboardsConfig(plugin, conditions)
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

    private fun FileConfiguration.getCompatibilityMode(path: String): CompatibilityMode {
        return CompatibilityMode.fromValue(this.getString(path, "disable"))
    }

    private fun FileConfiguration.getStorage(path: String): Storage {
        return Storage(
            this.getString("$path.driver", "h2"),
            this.getString("$path.tablePrefix", "simplescore_"),
            this.getString("$path.address", "127.0.0.1:3306"),
            this.getString("$path.database", "minecraft"),
            this.getString("$path.username", "simplescore"),
            this.getString("$path.password", "|D/-\\55\\^/0|2|)"),
            this.getStoragePool("$path.pool")
        )
    }

    private fun FileConfiguration.getStoragePool(path: String): Storage.Pool {
        return Storage.Pool(
            this.getInt("$path.maximumPoolSize", 8),
            this.getInt("$path.minimumIdle", 8),
            this.getLong("$path.maxLifetime", 1800000),
            this.getLong("$path.keepaliveTime", 0),
            this.getLong("$path.connectionTimeout", 5000),
            this.getConfigurationSection("$path.extraProperties")?.getValues(false) ?: emptyMap()
        )
    }
}