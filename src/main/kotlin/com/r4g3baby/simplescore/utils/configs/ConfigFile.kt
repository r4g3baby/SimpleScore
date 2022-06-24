package com.r4g3baby.simplescore.utils.configs

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

@Suppress("LeakingThis")
open class ConfigFile(plugin: Plugin, name: String) : File(plugin.dataFolder, "$name.yml") {
    lateinit var config: FileConfiguration
        private set

    init {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        if (!this.exists()) {
            if (plugin.getResource("$name.yml") != null) {
                plugin.saveResource("$name.yml", true)
            } else config = YamlConfiguration()
        }

        if (!this::config.isInitialized) {
            config = YamlConfiguration.loadConfiguration(InputStreamReader(FileInputStream(this), Charsets.UTF_8))
        }
    }
}