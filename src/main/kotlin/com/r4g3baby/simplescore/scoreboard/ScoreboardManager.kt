package com.r4g3baby.simplescore.scoreboard

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.ScoreboardWorld
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

class ScoreboardManager(private val plugin: SimpleScore) {
    fun createObjective(player: Player) {
        player.scoreboard = plugin.server.scoreboardManager.newScoreboard
        val objective = player.scoreboard.registerNewObjective(getPlayerIdentifier(player), "dummy")
        objective.displaySlot = DisplaySlot.SIDEBAR
    }

    fun removeObjective(player: Player) {
        getObjective(player)?.unregister()
    }

    fun hasObjective(player: Player): Boolean {
        return player.scoreboard != null &&
                player.scoreboard.getObjective(getPlayerIdentifier(player)) != null
    }

    fun getObjective(player: Player): Objective? {
        if (hasObjective(player)) {
            return player.scoreboard.getObjective(getPlayerIdentifier(player))
        }
        return null
    }

    fun hasScoreboard(world: World): Boolean {
        if (plugin.config != null) {
            if (plugin.config!!.worlds.containsKey(world.name) || plugin.config!!.shared.any { it.value.contains(world.name) }) {
                return true
            }
        }
        return false
    }

    fun getScoreboard(world: World): ScoreboardWorld? {
        if (plugin.config != null) {
            val worlds = plugin.config!!.worlds
            return if (!worlds.containsKey(world.name)) {
                val shared = plugin.config!!.shared.filter { it.value.contains(world.name) }
                if (!shared.isEmpty()) {
                    worlds[shared.keys.first()]
                } else {
                    null
                }
            } else {
                worlds[world.name]
            }
        }
        return null
    }

    private fun getPlayerIdentifier(player: Player): String {
        return player.uniqueId.toString().substring(0, 16)
    }
}