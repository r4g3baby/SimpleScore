package com.r4g3baby.simplescore.utils

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.codemc.worldguardwrapper.WorldGuardWrapper
import org.codemc.worldguardwrapper.flag.IWrappedFlag

object WorldGuardAPI {
    private lateinit var wrapper: WorldGuardWrapper
    private lateinit var scoreboardFlag: IWrappedFlag<String>

    var isEnabled = false
        private set

    fun init(plugin: Plugin) {
        if (plugin.server.pluginManager.getPlugin("WorldGuard") != null) {
            wrapper = WorldGuardWrapper.getInstance()

            var flag = wrapper.registerFlag("scoreboard", String::class.java, "")
            if (flag.isPresent) {
                scoreboardFlag = flag.get()
                isEnabled = true
            } else {
                flag = wrapper.getFlag("scoreboard", String::class.java)
                if (flag.isPresent) {
                    scoreboardFlag = flag.get()
                    isEnabled = true
                }
            }
        }
    }

    fun getFlag(player: Player, location: Location = player.location): List<String> {
        if (this::scoreboardFlag.isInitialized) {
            val flag = wrapper.queryFlag(player, location, scoreboardFlag)
            if (flag.isPresent) {
                return flag.get().split(",").map { it.trim() }
            }
        }
        return emptyList()
    }
}