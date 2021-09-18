package com.r4g3baby.simplescore

import com.r4g3baby.simplescore.commands.MainCmd
import com.r4g3baby.simplescore.configs.ConfigUpdater
import com.r4g3baby.simplescore.configs.MainConfig
import com.r4g3baby.simplescore.configs.MessagesConfig
import com.r4g3baby.simplescore.scoreboard.ScoreboardManager
import com.r4g3baby.simplescore.utils.WorldGuardAPI
import com.r4g3baby.simplescore.utils.updater.UpdateChecker
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class SimpleScore : JavaPlugin() {
    override fun onLoad() {
        WorldGuardAPI.init(this)
    }

    override fun onEnable() {
        Api.init(this)

        getCommand(name).executor = MainCmd()

        Metrics(this, 644)
        UpdateChecker(this, 23243) { new, _ ->
            if (new) {
                logger.warning("New version available download at:")
                logger.warning("https://www.spigotmc.org/resources/simplescore.23243/")
            }
        }
    }

    override fun onDisable() {
        scoreboardManager.disable()
    }

    companion object Api {
        lateinit var plugin: SimpleScore
            private set
        var usePlaceholderAPI = false
            private set
        var useMVdWPlaceholderAPI = false
            private set
        lateinit var config: MainConfig
            private set
        lateinit var messages: MessagesConfig
            private set
        lateinit var scoreboardManager: ScoreboardManager
            private set

        internal fun init(plugin: SimpleScore) {
            check(!this::plugin.isInitialized) { "SimpleScore has already been initialized." }
            this.plugin = plugin

            ConfigUpdater(plugin)

            usePlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
            useMVdWPlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")
            config = MainConfig(plugin)
            messages = MessagesConfig(plugin)
            scoreboardManager = ScoreboardManager()
        }

        fun reload() {
            config = MainConfig(plugin)
            messages = MessagesConfig(plugin)
            scoreboardManager.reload()
        }
    }
}