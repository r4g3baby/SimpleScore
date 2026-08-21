package com.r4g3baby.simplescore.bukkit.listener

import com.r4g3baby.simplescore.BukkitPlugin
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.user.UserDataRecalculateEvent

class LuckPermsListener(private val plugin: BukkitPlugin) {
    private val manager = plugin.manager

    init {
        LuckPermsProvider.get().eventBus.subscribe(
            UserDataRecalculateEvent::class.java
        ) { e ->
            plugin.server.getPlayer(e.user.uniqueId)?.let { player ->
                manager.getViewer(player.uniqueId)?.let { viewer ->
                    manager.updateViewerWorldScoreboard(viewer, player.world)
                    manager.updateViewerRegionScoreboard(viewer, player.location)
                }
            }
        }
    }
}
