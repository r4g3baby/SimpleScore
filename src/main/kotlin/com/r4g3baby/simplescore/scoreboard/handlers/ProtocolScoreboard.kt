package com.r4g3baby.simplescore.scoreboard.handlers

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftVersion
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent.fromChatMessage
import com.comphenix.protocol.wrappers.WrappedChatComponent.fromText
import com.r4g3baby.simplescore.scoreboard.models.PlayerBoard
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

class ProtocolScoreboard : ScoreboardHandler() {
    private val protocolManager = ProtocolLibrary.getProtocolManager()
    private val afterAquaticUpdate = MinecraftVersion.AQUATIC_UPDATE.atOrAbove()

    private val playerBoards = HashMap<UUID, PlayerBoard>()

    override fun createScoreboard(player: Player) {
        var packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE)
        packet.modifier.writeDefaults()
        packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
        packet.integers.write(0, 0) // Mode 0: Created Scoreboard
        if (afterAquaticUpdate) {
            packet.chatComponents.write(0, fromText(getPlayerIdentifier(player))) // Display Name
        } else packet.strings.write(1, getPlayerIdentifier(player)) // Display Name
        protocolManager.sendServerPacket(player, packet)

        packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE)
        packet.modifier.writeDefaults()
        packet.integers.write(0, 1) // Position 1: Sidebar
        packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
        protocolManager.sendServerPacket(player, packet)

        playerBoards[player.uniqueId] = PlayerBoard(getPlayerIdentifier(player), mapOf())
    }

    override fun removeScoreboard(player: Player) {
        var packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE)
        packet.modifier.writeDefaults()
        packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
        packet.integers.write(0, 1) // Mode 1: Remove Scoreboard
        protocolManager.sendServerPacket(player, packet)

        if (afterAquaticUpdate) {
            playerBoards.remove(player.uniqueId)?.scores?.forEach { (score, _) ->
                packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreToName(score)) // Team Name
                packet.integers.write(0, 1) // Mode - remove team
                protocolManager.sendServerPacket(player, packet)
            }
        } else playerBoards.remove(player.uniqueId)
    }

    override fun clearScoreboard(player: Player) {
        playerBoards[player.uniqueId]?.run {
            scores.forEach { (score, value) ->
                var scoreName = value
                if (afterAquaticUpdate) {
                    scoreName = scoreToName(score)

                    val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
                    packet.modifier.writeDefaults()
                    packet.strings.write(0, scoreName) // Team Name
                    packet.integers.write(0, 1) // Mode - remove team
                    protocolManager.sendServerPacket(player, packet)
                }

                val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreName) // Score Name
                packet.scoreboardActions.write(0, EnumWrappers.ScoreboardAction.REMOVE) // Action
                packet.strings.write(1, getPlayerIdentifier(player)) // Objective Name
                protocolManager.sendServerPacket(player, packet)
            }
            scores = emptyMap()
        }
    }

    override fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player) {
        playerBoards[player.uniqueId]?.let { playerBoard ->
            if (playerBoard.title != title) {
                val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE)
                packet.modifier.writeDefaults()
                packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
                packet.integers.write(0, 2) // Mode 2: Update Display Name
                if (afterAquaticUpdate) {
                    packet.chatComponents.write(0, fromChatMessage(title)[0]) // Display Name
                } else packet.strings.write(1, title) // Display Name
                protocolManager.sendServerPacket(player, packet)
            }

            scores.forEach { (score, value) ->
                val boardScore = playerBoard.getScore(value)
                if (boardScore == score) return@forEach

                var scoreName = value
                if (afterAquaticUpdate) {
                    scoreName = scoreToName(score)

                    val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
                    packet.modifier.writeDefaults()
                    packet.strings.write(0, scoreName) // Team Name
                    packet.chatComponents.write(0, fromText(scoreName)) // Display Name
                    packet.chatComponents.write(1, fromChatMessage(value)[0]) // Prefix
                    // packet.chatComponents.write(2, fromText("")) // Suffix

                    playerBoard.scores.containsKey(score).let { update ->
                        // there's no need to create the team again if this line already exists
                        if (update) {
                            packet.integers.write(0, 2) // Mode - update team info
                            protocolManager.sendServerPacket(player, packet)
                            return@forEach
                        }
                    }

                    packet.integers.write(0, 0) // Mode - create team
                    packet.getSpecificModifier(Collection::class.java).write(0, listOf(scoreName)) // Entities
                    protocolManager.sendServerPacket(player, packet)
                }

                val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreName) // Score Name
                packet.scoreboardActions.write(0, EnumWrappers.ScoreboardAction.CHANGE) // Action
                packet.strings.write(1, getPlayerIdentifier(player)) // Objective Name
                packet.integers.write(0, score) // Score Value
                protocolManager.sendServerPacket(player, packet)
            }

            playerBoard.scores.forEach { (score, value) ->
                var scoreName = value
                var remove = false
                if (afterAquaticUpdate && !scores.containsKey(score)) {
                    scoreName = scoreToName(score)

                    val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
                    packet.modifier.writeDefaults()
                    packet.strings.write(0, scoreName) // Team Name
                    packet.integers.write(0, 1) // Mode - remove team
                    protocolManager.sendServerPacket(player, packet)

                    remove = true
                }

                if (remove || (!afterAquaticUpdate && !scores.containsValue(value))) {
                    val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
                    packet.modifier.writeDefaults()
                    packet.strings.write(0, scoreName) // Score Name
                    packet.scoreboardActions.write(0, EnumWrappers.ScoreboardAction.REMOVE) // Action
                    packet.strings.write(1, getPlayerIdentifier(player)) // Objective Name
                    protocolManager.sendServerPacket(player, packet)
                }
            }

            playerBoard.let {
                it.title = title
                it.scores = scores
            }
        }
    }

    override fun hasScoreboard(player: Player): Boolean {
        return player.uniqueId in playerBoards
    }

    override fun hasLineLengthLimit(): Boolean {
        return !afterAquaticUpdate
    }

    private fun scoreToName(score: Int): String {
        return score.toString().toCharArray()
            .joinToString(ChatColor.COLOR_CHAR.toString(), ChatColor.COLOR_CHAR.toString())
    }
}