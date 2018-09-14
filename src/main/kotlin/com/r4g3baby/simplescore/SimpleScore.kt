package com.r4g3baby.simplescore

import com.r4g3baby.simplescore.commands.MainCmd
import com.r4g3baby.simplescore.configs.MainConfig
import com.r4g3baby.simplescore.configs.MessagesConfig
import com.r4g3baby.simplescore.scoreboard.ScoreboardManager
import com.r4g3baby.simplescore.scoreboard.listeners.ScoreboardListener
import com.r4g3baby.simplescore.scoreboard.tasks.ScoreboardTask
import com.r4g3baby.simplescore.utils.updater.UpdateChecker
import org.bstats.bukkit.MetricsLite
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Consumer

class SimpleScore : JavaPlugin() {
    lateinit var config: MainConfig
        private set
    lateinit var messagesConfig: MessagesConfig
        private set
    lateinit var scoreboardManager: ScoreboardManager
        private set
    var placeholderAPI: Boolean = false
        private set

    override fun onEnable() {
        load(true)

        MetricsLite(this)
        UpdateChecker(this, 23243, Consumer {
            logger.warning("New version available download at:")
            logger.warning(it)
        })
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
        HandlerList.unregisterAll(this)
    }

    fun load(firstLoad: Boolean = false) {
        config = MainConfig(this)
        messagesConfig = MessagesConfig(this)
        scoreboardManager = ScoreboardManager(this)
        placeholderAPI = server.pluginManager.isPluginEnabled("PlaceholderAPI")

        if (firstLoad) {
            getCommand(name).executor = MainCmd(this)
            server.pluginManager.registerEvents(ScoreboardListener(this), this)
        } else server.scheduler.cancelTasks(this)

        server.scheduler.runTaskTimerAsynchronously(this, ScoreboardTask(this), 20L, config.updateTime.toLong())

        server.onlinePlayers
                .filter { scoreboardManager.hasScoreboard(it.world) }
                .forEach { scoreboardManager.createObjective(it) }
    }
}