package com.r4g3baby.simplescore.scoreboard.listeners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListeningWhitelist
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.events.PacketListener
import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.handlers.ScoreboardHandler.Companion.getPlayerIdentifier
import org.bukkit.plugin.Plugin

class PacketsListener : PacketListener {
    private val listeningPackets = ListeningWhitelist.newBuilder().monitor().types(
        PacketType.Play.Server.SCOREBOARD_OBJECTIVE, PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE
    ).build()

    override fun onPacketSending(e: PacketEvent) {
        if (e.packet.type == PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE) {
            val position = e.packet.integers.read(0)
            val name = e.packet.strings.read(0)
            if (position == 1 && name != getPlayerIdentifier(e.player)) {
                SimpleScore.plugin.logger.info("Detected scoreboard override for player ${e.player.name}.")
                SimpleScore.manager.playersData.setDisabled(SimpleScore.plugin, e.player, true)
            }
        } else if (e.packet.type == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
            val name = e.packet.strings.read(0)
            if (name != getPlayerIdentifier(e.player)) {
                val mode = e.packet.integers.read(0)
                if (mode == 1 && SimpleScore.manager.playersData.isDisabling(SimpleScore.plugin, e.player)) {
                    SimpleScore.plugin.logger.info("Restored scoreboard for player ${e.player.name}.")
                    SimpleScore.manager.playersData.setDisabled(SimpleScore.plugin, e.player, false)
                }
            }
        }
    }

    override fun onPacketReceiving(e: PacketEvent) {}

    override fun getSendingWhitelist(): ListeningWhitelist {
        return listeningPackets
    }

    override fun getReceivingWhitelist(): ListeningWhitelist {
        return ListeningWhitelist.EMPTY_WHITELIST
    }

    override fun getPlugin(): Plugin {
        return SimpleScore.plugin
    }
}