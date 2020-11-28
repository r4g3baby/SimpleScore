package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.ChatColor

class MessagesConfig(plugin: SimpleScore) : ConfigFile(plugin, "messages") {
    val help = color(config.getString("Help", "&6SimpleScore &7plugin made by &cR4G3_BABY\n&8/SB reload &7to reload the plugin\n&8/SB toggle &7to toggle the scoreboard"))

    private val prefix = color(config.getString("Prefix", "&8[&bSimpleScore&8] "))
    val permission = prefix + color(config.getString("Permission", "&cYou don't have permission to execute this command."))
    val onlyPlayers = prefix + color(config.getString("OnlyPlayers", "&cOnly players can execute this command."))
    val notOnline = prefix + color(config.getString("NotOnline", "&cThe specified player is not online."))
    val reloading = prefix + color(config.getString("Reloading", "&6Reloading plugin..."))
    val reloaded = prefix + color(config.getString("Reloaded", "&aPlugin reloaded."))
    val enabled = prefix + color(config.getString("Enabled", "&aScoreboard enabled."))
    val enabledOther = prefix + color(config.getString("EnabledOther", "&aScoreboard enabled for %s."))
    val disabled = prefix + color(config.getString("Disabled", "&cScoreboard disabled."))
    val disabledOther = prefix + color(config.getString("DisabledOther", "&cScoreboard disabled for %s."))
    val checkingForUpdates = prefix + color(config.getString("CheckingForUpdates", "&6You are running version &7v%s&6, checking for updates..."))
    val foundNewUpdate = prefix + color(config.getString("FoundNewUpdate", "&6New update found &7v%s&6, download at:\n&6https://www.spigotmc.org/resources/simplescore.23243/"))
    val runningLatest = prefix + color(config.getString("RunningLatest", "&aYou are running the latest plugin version."))

    private fun color(text: String): String = ChatColor.translateAlternateColorCodes('&', text)
}