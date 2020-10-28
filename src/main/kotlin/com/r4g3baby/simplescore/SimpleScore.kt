package com.r4g3baby.simplescore

import com.r4g3baby.simplescore.commands.MainCmd
import com.r4g3baby.simplescore.configs.MainConfig
import com.r4g3baby.simplescore.configs.MessagesConfig
import com.r4g3baby.simplescore.scoreboard.ScoreboardManager
import com.r4g3baby.simplescore.utils.WorldGuardAPI
import com.r4g3baby.simplescore.utils.updater.UpdateChecker
import org.bstats.bukkit.MetricsLite
import org.bukkit.plugin.java.JavaPlugin

class SimpleScore : JavaPlugin() {
    lateinit var config: MainConfig
        private set
    lateinit var messagesConfig: MessagesConfig
        private set
    lateinit var scoreboardManager: ScoreboardManager
        private set

    var worldGuard: Boolean = false
        private set
    var placeholderAPI: Boolean = false
        private set

    override fun onLoad() {
        worldGuard = WorldGuardAPI.init(this)
    }

    override fun onEnable() {
        reload(true)

        MetricsLite(this, 644)
        UpdateChecker(this, 23243) {
            logger.warning("New version available download at:")
            logger.warning(it)
        }
    }

    override fun onDisable() {
        scoreboardManager.disable()
    }

    fun reload(firstLoad: Boolean = false) {
        config = MainConfig(this)
        messagesConfig = MessagesConfig(this)
        placeholderAPI = server.pluginManager.isPluginEnabled("PlaceholderAPI")

        if (firstLoad) {
            scoreboardManager = ScoreboardManager(this)

            getCommand(name).executor = MainCmd(this)
        } else scoreboardManager.reload()

        server.onlinePlayers.forEach { scoreboardManager.clearScoreboard(it) }
    }
}