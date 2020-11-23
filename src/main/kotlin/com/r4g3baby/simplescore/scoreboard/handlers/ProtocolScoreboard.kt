package com.r4g3baby.simplescore.scoreboard.handlers

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftVersion
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

class ProtocolScoreboard : ScoreboardHandler() {
    private val protocolManager = ProtocolLibrary.getProtocolManager()

    private val playerTitles = HashMap<UUID, String>()
    private val playerBoards = HashMap<UUID, Map<String, Int>>()

    override fun createScoreboard(player: Player) {
        var packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE)
        packet.modifier.writeDefaults()
        packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
        packet.integers.write(0, 0) // Mode 0: Created Scoreboard
        if (MinecraftVersion.AQUATIC_UPDATE.atOrAbove()) {
            packet.chatComponents.write(0, WrappedChatComponent.fromText(getPlayerIdentifier(player))) // Display Name
        } else packet.strings.write(1, getPlayerIdentifier(player)) // Display Name
        protocolManager.sendServerPacket(player, packet)

        packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE)
        packet.modifier.writeDefaults()
        packet.integers.write(0, 1) // Position 1: Sidebar
        packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
        protocolManager.sendServerPacket(player, packet)
    }

    override fun removeScoreboard(player: Player) {
        val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE)
        packet.modifier.writeDefaults()
        packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
        packet.integers.write(0, 1) // Mode 1: Remove Scoreboard
        protocolManager.sendServerPacket(player, packet)

        playerTitles.remove(player.uniqueId)
        playerBoards.remove(player.uniqueId)
    }

    override fun clearScoreboard(player: Player) {
        playerBoards.remove(player.uniqueId)?.forEach { (value, _) ->
            val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
            packet.modifier.writeDefaults()
            packet.strings.write(0, value) // Score Name
            packet.scoreboardActions.write(0, EnumWrappers.ScoreboardAction.REMOVE) // Action
            packet.strings.write(1, getPlayerIdentifier(player)) // Objective Name
            protocolManager.sendServerPacket(player, packet)
        }
    }

    override fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player) {
        if (playerTitles[player.uniqueId] != title) {
            val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE)
            packet.modifier.writeDefaults()
            packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
            packet.integers.write(0, 2) // Mode 2: Update Display Name
            if (MinecraftVersion.AQUATIC_UPDATE.atOrAbove()) {
                packet.chatComponents.write(0, WrappedChatComponent.fromText(title)) // Display Name
            } else packet.strings.write(1, title) // Display Name
            protocolManager.sendServerPacket(player, packet)

            playerTitles[player.uniqueId] = title
        }

        val playerBoard = playerBoards[player.uniqueId]
        scores.forEach { (score, value) ->
            val boardScore = playerBoard?.get(value)
            if (boardScore == score) return@forEach

            val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
            packet.modifier.writeDefaults()
            packet.strings.write(0, value) // Score Name
            packet.scoreboardActions.write(0, EnumWrappers.ScoreboardAction.CHANGE) // Action
            packet.strings.write(1, getPlayerIdentifier(player)) // Objective Name
            packet.integers.write(0, score) // Score Value
            protocolManager.sendServerPacket(player, packet)
        }

        playerBoard?.filter { !scores.values.contains(it.key) }?.forEach { (value, _) ->
            val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
            packet.modifier.writeDefaults()
            packet.strings.write(0, value) // Score Name
            packet.scoreboardActions.write(0, EnumWrappers.ScoreboardAction.REMOVE) // Action
            packet.strings.write(1, getPlayerIdentifier(player)) // Objective Name
            protocolManager.sendServerPacket(player, packet)
        }

        playerBoards[player.uniqueId] = scores.map { (key, value) -> value to key }.toMap()
    }
}