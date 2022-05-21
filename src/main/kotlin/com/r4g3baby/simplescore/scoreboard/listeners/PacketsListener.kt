package com.r4g3baby.simplescore.scoreboard.listeners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListeningWhitelist
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.events.PacketListener
import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.CompatibilityMode
import com.r4g3baby.simplescore.scoreboard.handlers.ScoreboardHandler.Companion.getPlayerIdentifier
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class PacketsListener : PacketListener, Listener {
    private val listeningPackets = ListeningWhitelist.newBuilder().highest().types(
        PacketType.Play.Server.SCOREBOARD_OBJECTIVE, PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE
    ).build()

    override fun getSendingWhitelist(): ListeningWhitelist {
        return listeningPackets
    }

    override fun onPacketSending(e: PacketEvent) {
        if (e.isCancelled) return

        when (SimpleScore.config.compatibilityMode) {
            CompatibilityMode.DISABLE -> {
                if (e.packet.type == PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE) {
                    val position = e.packet.integers.read(0)
                    val name = e.packet.strings.read(0)
                    if (position == 1 && name != getPlayerIdentifier(e.player)) {
                        val protocolPlugin = ProtocolLibrary.getPlugin()
                        SimpleScore.manager.playersData.setDisabled(protocolPlugin, e.player, true)
                    }
                } else if (e.packet.type == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
                    val name = e.packet.strings.read(0)
                    if (name != getPlayerIdentifier(e.player)) {
                        val mode = e.packet.integers.read(0)
                        val protocolPlugin = ProtocolLibrary.getPlugin()
                        if (mode == 1 && SimpleScore.manager.playersData.isDisabling(protocolPlugin, e.player)) {
                            SimpleScore.manager.playersData.setDisabled(protocolPlugin, e.player, false)
                        }
                    }
                }
            }
            CompatibilityMode.BLOCK -> {
                if (e.packet.type == PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE) {
                    val position = e.packet.integers.read(0)
                    val name = e.packet.strings.read(0)
                    if (position == 1 && name != getPlayerIdentifier(e.player)) {
                        e.isCancelled = SimpleScore.manager.needsScoreboard(e.player)
                    }
                }
            }
            else -> {}
        }
    }

    // EventPriority set to LOW, so it gets called before the onPlayerQuit event from PlayersListener
    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        SimpleScore.manager.playersData.setDisabled(ProtocolLibrary.getPlugin(), e.player, false)
    }

    override fun getReceivingWhitelist(): ListeningWhitelist {
        return ListeningWhitelist.EMPTY_WHITELIST
    }

    override fun onPacketReceiving(e: PacketEvent) {}

    override fun getPlugin(): Plugin {
        return SimpleScore.plugin
    }
}