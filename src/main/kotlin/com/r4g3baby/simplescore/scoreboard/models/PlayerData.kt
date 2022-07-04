package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.plugin.Plugin
import java.util.UUID

data class PlayerData(
    val uniqueId: UUID,
    private val pluginsHiding: MutableSet<Plugin> = HashSet(),
    private val pluginsDisabling: MutableSet<Plugin> = HashSet(),
    private val pluginsScoreboard: MutableMap<Plugin, String> = LinkedHashMap()
) {
    internal var scoreboard: PlayerScoreboard? = null

    val scoreboards get() = pluginsScoreboard.map { it.value }

    val isHidden get() = pluginsHiding.isNotEmpty()
    val isDisabled get() = pluginsDisabling.isNotEmpty()
    val hasScoreboards get() = pluginsScoreboard.isNotEmpty()

    internal fun hide(plugin: Plugin): Boolean {
        return pluginsHiding.add(plugin)
    }

    internal fun show(plugin: Plugin): Boolean {
        return pluginsHiding.remove(plugin)
    }

    fun isHiding(plugin: Plugin): Boolean {
        return plugin in pluginsHiding
    }

    internal fun disable(plugin: Plugin): Boolean {
        return pluginsDisabling.add(plugin)
    }

    internal fun enable(plugin: Plugin): Boolean {
        return pluginsDisabling.remove(plugin)
    }

    fun isDisabling(plugin: Plugin): Boolean {
        return plugin in pluginsDisabling
    }

    internal fun setScoreboard(plugin: Plugin, scoreboard: String?) {
        if (scoreboard == null) {
            pluginsScoreboard.remove(plugin)
        } else pluginsScoreboard[plugin] = scoreboard
    }

    fun getScoreboard(plugin: Plugin): String? {
        return pluginsScoreboard[plugin]
    }

    fun hasScoreboard(plugin: Plugin): Boolean {
        return plugin in pluginsScoreboard
    }
}
