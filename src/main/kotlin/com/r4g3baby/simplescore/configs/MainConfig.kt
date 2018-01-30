package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.plugin.java.JavaPlugin

class MainConfig(plugin: JavaPlugin) : ConfigFile(plugin, "config") {
    init {
        println(config.get("test", "Bye"))
    }
}