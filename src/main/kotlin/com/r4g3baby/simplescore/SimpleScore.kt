package com.r4g3baby.simplescore

import com.r4g3baby.simplescore.commands.MainCmd
import com.r4g3baby.simplescore.configs.ConfigUpdater
import com.r4g3baby.simplescore.configs.MainConfig
import com.r4g3baby.simplescore.configs.lang.I18n
import com.r4g3baby.simplescore.scoreboard.ScoreboardManager
import com.r4g3baby.simplescore.scoreboard.worldguard.WorldGuardAPI
import com.r4g3baby.simplescore.storage.StorageManager
import com.r4g3baby.simplescore.utils.updater.UpdateChecker
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class SimpleScore : JavaPlugin() {
    override fun onLoad() {
        WorldGuardAPI.init()
    }

    override fun onEnable() {
        Api.init(this)

        getCommand(name).executor = MainCmd()

        Metrics(this, 644)

        @Suppress("RedundantCompanionReference")
        if (Api.config.checkForUpdates) {
            UpdateChecker(this, pluginId) { hasUpdate, newVersion ->
                if (hasUpdate) {
                    logger.warning("New version (v$newVersion) available, download at:")
                    logger.warning("https://www.spigotmc.org/resources/$pluginId/")
                }
            }
        }
    }

    override fun onDisable() {
        disable()
    }

    companion object Api {
        const val pluginId = 23243

        lateinit var plugin: SimpleScore
            private set
        var usePlaceholderAPI = false
            private set
        var useMVdWPlaceholderAPI = false
            private set
        var isViaBackwardsEnabled = false
            private set
        lateinit var config: MainConfig
            private set
        lateinit var i18n: I18n
            private set
        lateinit var storage: StorageManager
            private set
        lateinit var manager: ScoreboardManager
            private set

        internal fun init(plugin: SimpleScore) {
            check(!this::plugin.isInitialized) { "SimpleScore has already been initialized." }
            this.plugin = plugin

            ConfigUpdater(plugin)

            usePlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
            useMVdWPlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")
            isViaBackwardsEnabled = Bukkit.getPluginManager().isPluginEnabled("ViaBackwards")
            config = MainConfig(plugin)
            i18n = I18n(config.language, plugin)
            storage = StorageManager()
            manager = ScoreboardManager()
        }

        fun reload() {
            config = MainConfig(plugin)
            i18n.loadTranslations(config.language)
            manager.reload()
        }

        internal fun disable() {
            if (this::storage.isInitialized) {
                storage.shutdown()
            }
        }
    }
}