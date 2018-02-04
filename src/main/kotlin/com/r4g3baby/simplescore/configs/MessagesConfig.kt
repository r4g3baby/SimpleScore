package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.ChatColor

class MessagesConfig(plugin: SimpleScore) : ConfigFile(plugin, "messages") {
    val help = color(config.getString("Help", "&cN/A"))
    val permission = color(config.getString("Permission", "&cN/A"))
    val reloading = color(config.getString("Reloading", "&cN/A"))
    val reloaded = color(config.getString("Reloaded", "&cN/A"))

    private fun color(text: String): String = ChatColor.translateAlternateColorCodes('&', text)
}