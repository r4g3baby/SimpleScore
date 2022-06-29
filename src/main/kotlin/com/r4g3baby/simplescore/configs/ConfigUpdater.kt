package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin
import java.io.File

class ConfigUpdater(plugin: Plugin) {
    private val mainConfigFile = ConfigFile(plugin, "config")
    private val mainConfig = mainConfigFile.config

    private val scoreboardsConfigFile = ConfigFile(plugin, "scoreboards")
    private val scoreboardsConfig = scoreboardsConfigFile.config

    private val conditionsConfigFile = ConfigFile(plugin, "conditions")
    private val conditionsConfig = conditionsConfigFile.config

    private var version = mainConfig.getInt("version", -1)

    init {
        var firstRun = true
        var updateFunc = getUpdateFunc()
        while (updateFunc != null) {
            if (firstRun) {
                plugin.logger.info("Detected an old config format (v$version), the plugin will create a backup and attempt to update it.")
                mainConfigFile.copyTo(File(mainConfigFile.parentFile, "config.bak"), true)
                firstRun = false
            }

            version = updateFunc()

            mainConfig.set("version", version)
            mainConfig.save(mainConfigFile)

            plugin.logger.info("Config updated to version $version.")

            updateFunc = getUpdateFunc()
        }
    }

    private fun getUpdateFunc(): (() -> Int)? {
        return when (version) {
            -1, 0 -> return version@{
                mainConfig.set("language", "en")
                mainConfig.set("checkForUpdates", true)

                val updateTime = mainConfig.getInt("UpdateTime", 20)
                if (mainConfig.contains("UpdateTime")) {
                    mainConfig.set("UpdateTime", null)
                }

                if (mainConfig.contains("SaveScoreboards")) {
                    mainConfig.set("savePlayerData", mainConfig.get("SaveScoreboards"))
                    mainConfig.set("SaveScoreboards", null)
                }

                if (mainConfig.contains("AsyncPlaceholders")) {
                    mainConfig.set("asyncPlaceholders", mainConfig.get("AsyncPlaceholders"))
                    mainConfig.set("AsyncPlaceholders", null)
                }

                if (mainConfig.contains("ForceLegacy")) {
                    mainConfig.set("forceLegacy", mainConfig.get("ForceLegacy"))
                    mainConfig.set("ForceLegacy", null)
                }

                if (mainConfig.isConfigurationSection("Scoreboards")) {
                    val scoreboardsSection = mainConfig.getConfigurationSection("Scoreboards")
                    for (scoreboard in scoreboardsSection.getKeys(false)) {
                        scoreboardsConfig.set(scoreboard, scoreboardsSection.getConfigurationSection(scoreboard))

                        scoreboardsConfig.getConfigurationSection(scoreboard).apply {
                            if (updateTime != 20) {
                                set("updateTime", updateTime)
                            }

                            if (getBoolean("Restricted", false)) {
                                val condition = "sb$scoreboard"
                                conditionsConfig.createSection(
                                    condition, mapOf(
                                        "type" to "HAS_PERMISSION",
                                        "permission" to "simplescore.${scoreboard.lowercase()}"
                                    )
                                )

                                set("conditions", listOf(condition))
                                set("Restricted", null)
                            }

                            set("titles", get("Titles"))
                            set("Titles", null)

                            set("scores", get("Scores"))
                            set("Scores", null)
                        }
                    }

                    mainConfig.set("Scoreboards", null)
                    conditionsConfig.save(conditionsConfigFile)
                    scoreboardsConfig.save(scoreboardsConfigFile)
                }

                if (mainConfig.isConfigurationSection("Worlds")) {
                    mainConfig.set("worlds", mainConfig.getConfigurationSection("Worlds"))
                    mainConfig.set("Worlds", null)
                }

                return@version 1
            }
            1 -> return version@{
                val storageDriver = if (mainConfig.getBoolean("savePlayerData", false)) "h2" else "none"
                mainConfig.set("savePlayerData", null)

                mainConfig.createSection(
                    "storage", mapOf(
                        "driver" to storageDriver,
                        "tablePrefix" to "simplescore_",
                        "address" to "127.0.0.1:3306",
                        "database" to "minecraft",
                        "username" to "simplescore",
                        "password" to "|D/-\\55\\^/0|2|)",
                        "pool" to mapOf(
                            "maximumPoolSize" to 8,
                            "minimumIdle" to 8,
                            "maxLifetime" to 1800000,
                            "keepaliveTime" to 0,
                            "connectionTimeout" to 5000,
                            "extraProperties" to mapOf(
                                "useUnicode" to true,
                                "characterEncoding" to "utf8"
                            )
                        )
                    )
                )

                return@version 2
            }
            2 -> return version@{
                scoreboardsConfig.getKeys(false).forEach { scoreboard ->
                    scoreboardsConfig.getConfigurationSection(scoreboard).apply {
                        updateFrames("titles")

                        if (isConfigurationSection("scores")) getConfigurationSection("scores").apply {
                            getKeys(false).forEach { score -> updateFrames(score)}
                        }
                    }
                }

                scoreboardsConfig.save(scoreboardsConfigFile)

                return@version 3
            }
            else -> null
        }
    }

    private fun ConfigurationSection.updateFrames(path: String) {
        when {
            isConfigurationSection(path) -> getConfigurationSection(path).let { section ->
                if (section.isList("frames")) {
                    val newList = ArrayList<Any?>()
                    section.getList("frames").forEach { frame -> newList.add(updateFrame(frame)) }
                    section.set("frames", newList)
                }
            }
            isList(path) -> {
                val newList = ArrayList<Any?>()
                getList(path).forEach { frame -> newList.add(updateFrame(frame)) }
                set(path, newList)
            }
        }
    }

    private fun updateFrame(frame: Any?): Any? {
        if (frame is MutableMap<*, *>) {
            val time = frame.remove("time")
            if (time != null) {
                return frame.toMutableMap().apply { put("update", time) }
            }
        }
        return frame
    }
}