package com.r4g3baby.simplescore

import com.r4g3baby.simplescore.utils.updater.UpdateChecker
import org.bstats.bukkit.MetricsLite
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Consumer

class SimpleScore : JavaPlugin() {

    override fun onEnable() {
        println("Enabling")

        MetricsLite(this)
        UpdateChecker(this, 23243, Consumer {
            logger.warning("New version available download at:")
            logger.warning(it)
        })
    }

    override fun onDisable() {
        println("Disabling")
    }
}