package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.ChatColor

class MessagesConfig(plugin: SimpleScore) : ConfigFile(plugin, "messages") {
    val help = color(config.getString("Help", "&cN/A"))

    private val prefix = color(config.getString("Prefix", "&8[&bSimpleScore&8] "))
    val permission = prefix + color(config.getString("Permission", "&cN/A"))
    val onlyPlayers = prefix + color(config.getString("OnlyPlayers", "&cN/A"))
    val reloading = prefix + color(config.getString("Reloading", "&cN/A"))
    val reloaded = prefix + color(config.getString("Reloaded", "&cN/A"))
    val enabled = prefix + color(config.getString("Enabled", "&cN/A"))
    val disabled = prefix + color(config.getString("Disabled", "&cN/A"))

    private fun color(text: String): String = ChatColor.translateAlternateColorCodes('&', text)
}