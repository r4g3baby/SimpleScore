package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.plugin.Plugin

data class PlayerData(
    val pluginsHiding: MutableSet<Plugin> = HashSet(),
    val pluginsDisabling: MutableSet<Plugin> = HashSet(),
    val pluginsScoreboard: LinkedHashMap<Plugin, Scoreboard> = LinkedHashMap()
) {
    val scoreboards get() = pluginsScoreboard.map { it.value }

    val isHidden get() = pluginsHiding.size > 0
    val isDisabled get() = pluginsDisabling.size > 0
    val hasScoreboards get() = pluginsScoreboard.size > 0

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

    fun setScoreboard(plugin: Plugin, scoreboard: Scoreboard?) {
        if (scoreboard == null) {
            pluginsScoreboard.remove(plugin)
        } else pluginsScoreboard[plugin] = scoreboard
    }

    fun getScoreboard(plugin: Plugin): Scoreboard? {
        return pluginsScoreboard[plugin]
    }

    fun hasScoreboard(plugin: Plugin): Boolean {
        return plugin in pluginsScoreboard
    }
}
