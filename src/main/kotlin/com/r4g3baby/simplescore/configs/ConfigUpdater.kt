package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import java.io.File

class ConfigUpdater(plugin: SimpleScore) {
    private val mainConfigFile = ConfigFile(plugin, "config")
    private val mainConfig = mainConfigFile.config

    private val scoreboardsConfigFile = ConfigFile(plugin, "scoreboards")
    private val scoreboardsConfig = scoreboardsConfigFile.config

    init {
        val version = mainConfig.getInt("version", -1)
        if (version < 0) {
            plugin.logger.info("Detected an old config format, the plugin will create a backup and attempt to update it.")
            mainConfigFile.copyTo(File(mainConfigFile.parentFile, "config.bak"), true)

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
                            set("permission", scoreboard.lowercase())
                            set("Restricted", null)
                        }

                        set("titles", get("Titles"))
                        set("Titles", null)

                        set("scores", get("Scores"))
                        set("Scores", null)
                    }
                }
                mainConfig.set("Scoreboards", null)
                scoreboardsConfig.save(scoreboardsConfigFile)
            }

            if (mainConfig.isConfigurationSection("Worlds")) {
                mainConfig.set("worlds", mainConfig.getConfigurationSection("Worlds"))
                mainConfig.set("Worlds", null)
            }

            mainConfig.set("version", 1)
            mainConfig.save(mainConfigFile)
        }
    }
}