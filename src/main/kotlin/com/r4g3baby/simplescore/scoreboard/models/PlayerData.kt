package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.plugin.Plugin

data class PlayerData(
    var scoreboard: Scoreboard? = null,
    val pluginsHiding: MutableSet<Plugin> = HashSet(),
    val pluginsDisabling: MutableSet<Plugin> = HashSet()
) {
    val hasScoreboard get() = scoreboard != null

    val isHidden get() = pluginsHiding.size > 0
    val isDisabled get() = pluginsDisabling.size > 0

    fun hide(plugin: Plugin): Boolean {
        return pluginsHiding.add(plugin)
    }

    fun show(plugin: Plugin): Boolean {
        return pluginsHiding.remove(plugin)
    }

    fun isHiding(plugin: Plugin): Boolean {
        return plugin in pluginsHiding
    }

    fun disable(plugin: Plugin): Boolean {
        return pluginsDisabling.add(plugin)
    }

    fun enable(plugin: Plugin): Boolean {
        return pluginsDisabling.remove(plugin)
    }

    fun isDisabling(plugin: Plugin): Boolean {
        return plugin in pluginsDisabling
    }
}
