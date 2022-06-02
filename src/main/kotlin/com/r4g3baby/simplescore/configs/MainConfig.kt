package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.scoreboard.models.CompatibilityMode
import com.r4g3baby.simplescore.storage.models.Storage
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.configuration.ConfigurationSection
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

    private val _worlds = LinkedHashMap<Predicate<String>, List<String>>()
    val worlds: Map<Predicate<String>, List<String>> get() = _worlds

    init {
        if (config.isConfigurationSection("worlds")) {
            val worldsSec = config.getConfigurationSection("worlds")
            worldsSec.getKeys(false).forEach { world ->
                val pattern = Pattern.compile("^${world}$", Pattern.CASE_INSENSITIVE)
                _worlds[pattern.asPredicate()] = when {
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

    private fun ConfigurationSection.getCompatibilityMode(path: String): CompatibilityMode {
        return CompatibilityMode.fromValue(getString(path, "disable"))
    }

    private fun ConfigurationSection.getStorage(path: String): Storage {
        return Storage(
            getString("$path.driver", "h2"),
            getString("$path.tablePrefix", "simplescore_"),
            getString("$path.address", "127.0.0.1:3306"),
            getString("$path.database", "minecraft"),
            getString("$path.username", "simplescore"),
            getString("$path.password", "|D/-\\55\\^/0|2|)"),
            getStoragePool("$path.pool")
        )
    }

    private fun ConfigurationSection.getStoragePool(path: String): Storage.Pool {
        return Storage.Pool(
            getInt("$path.maximumPoolSize", 8),
            getInt("$path.minimumIdle", 8),
            getLong("$path.maxLifetime", 1800000),
            getLong("$path.keepaliveTime", 0),
            getLong("$path.connectionTimeout", 5000),
            getConfigurationSection("$path.extraProperties")?.getValues(false) ?: emptyMap()
        )
    }
}