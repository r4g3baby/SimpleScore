package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.ChatColor

class MessagesConfig(plugin: SimpleScore) : ConfigFile(plugin, "messages") {
    val help = color(config.getString("Help", "&cN/A"))
    val permission = color(config.getString("Permission", "&cN/A"))
    val onlyPlayers = color(config.getString("OnlyPlayers", "&cN/A"))
    val reloading = color(config.getString("Reloading", "&cN/A"))
    val reloaded = color(config.getString("Reloaded", "&cN/A"))
    val enabled = color(config.getString("Enabled", "&cN/A"))
    val disabled = color(config.getString("Disabled", "&cN/A"))

    private fun color(text: String): String = ChatColor.translateAlternateColorCodes('&', text)
}