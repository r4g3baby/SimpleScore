package com.r4g3baby.simplescore.scoreboard.handlers

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.InternalStructure
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftVersion
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent.fromLegacyText
import com.comphenix.protocol.wrappers.WrappedChatComponent.fromText
import com.r4g3baby.simplescore.scoreboard.models.PlayerBoard
import org.bukkit.entity.Player
import java.util.*

class ProtocolScoreboard : ScoreboardHandler() {
    private val protocolManager = ProtocolLibrary.getProtocolManager()

    // Don't use ProtocolLib version enums, so we can support older plugin versions
    private val afterAquaticUpdate = MinecraftVersion("1.13").atOrAbove()
    private val afterCavesAndCliffsUpdate = MinecraftVersion("1.17").atOrAbove()

    private val playerBoards = HashMap<UUID, PlayerBoard>()

    override fun createScoreboard(player: Player) {
        playerBoards.computeIfAbsent(player.uniqueId) {
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

            return@computeIfAbsent PlayerBoard(getPlayerIdentifier(player), mapOf())
        }
    }

    override fun removeScoreboard(player: Player) {
        playerBoards.remove(player.uniqueId)?.also { playerBoard ->
            var packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE)
            packet.modifier.writeDefaults()
            packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
            packet.integers.write(0, 1) // Mode 1: Remove Scoreboard
            protocolManager.sendServerPacket(player, packet)

            playerBoard.scores.forEach { (score, _) ->
                packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreToName(score)) // Team Name
                if (afterAquaticUpdate) {
                    packet.integers.write(0, 1) // Mode - remove team
                } else packet.integers.write(1, 1) // Mode - remove team
                protocolManager.sendServerPacket(player, packet)
            }
        }
    }

    override fun clearScoreboard(player: Player) {
        playerBoards[player.uniqueId]?.also { playerBoard ->
            playerBoard.scores.forEach { (score, _) ->
                val scoreName = scoreToName(score)

                var packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreName) // Team Name
                if (afterAquaticUpdate) {
                    packet.integers.write(0, 1) // Mode - remove team
                } else packet.integers.write(1, 1) // Mode - remove team
                protocolManager.sendServerPacket(player, packet)

                packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreName) // Score Name
                packet.scoreboardActions.write(0, EnumWrappers.ScoreboardAction.REMOVE) // Action
                packet.strings.write(1, getPlayerIdentifier(player)) // Objective Name
                protocolManager.sendServerPacket(player, packet)
            }
            playerBoard.scores = emptyMap()
        }
    }

    override fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player) {
        playerBoards[player.uniqueId]?.also { playerBoard ->
            if (playerBoard.title != title) {
                val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE)
                packet.modifier.writeDefaults()
                packet.strings.write(0, getPlayerIdentifier(player)) // Objective Name
                packet.integers.write(0, 2) // Mode 2: Update Display Name
                if (afterAquaticUpdate) {
                    packet.chatComponents.write(0, fromLegacyText(title)) // Display Name
                } else {
                    val displayTitle = if (title.length > 32) title.substring(0, 32) else title
                    packet.strings.write(1, displayTitle) // Display Name
                }
                protocolManager.sendServerPacket(player, packet)
            }

            scores.forEach { (score, value) ->
                val boardScore = playerBoard.getScore(value)
                if (boardScore == score) return@forEach

                val scoreName = scoreToName(score)

                var packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreName) // Team Name

                // Always split at 16 to improve version compatibility (players on 1.12 and older)
                val splitText = splitScoreLine(value, 16, !afterAquaticUpdate)
                if (afterCavesAndCliffsUpdate) {
                    val optStruct: Optional<InternalStructure> = packet.optionalStructures.read(0)
                    if (optStruct.isPresent) {
                        val struct = optStruct.get()
                        struct.chatComponents.write(0, fromText(scoreName)) // Display Name
                        struct.chatComponents.write(1, fromLegacyText(splitText.first)) // Prefix
                        struct.chatComponents.write(2, fromLegacyText(splitText.second)) // Suffix

                        packet.optionalStructures.write(0, Optional.of(struct))
                    }
                } else if (afterAquaticUpdate) {
                    packet.chatComponents.write(0, fromText(scoreName)) // Display Name
                    packet.chatComponents.write(1, fromLegacyText(splitText.first)) // Prefix
                    packet.chatComponents.write(2, fromLegacyText(splitText.second)) // Suffix
                } else {
                    packet.strings.write(1, scoreName) // Display Name
                    packet.strings.write(2, splitText.first) // Prefix
                    packet.strings.write(3, splitText.second) // Suffix
                }

                // there's no need to create the team again if this line already exists
                if (playerBoard.scores.containsKey(score)) {
                    if (afterAquaticUpdate) {
                        packet.integers.write(0, 2) // Mode - update team info
                    } else packet.integers.write(1, 2) // Mode - update team info
                    protocolManager.sendServerPacket(player, packet)
                    return@forEach
                }

                if (afterAquaticUpdate) {
                    packet.integers.write(0, 0) // Mode - create team
                } else packet.integers.write(1, 0) // Mode - create team
                packet.getSpecificModifier(Collection::class.java).write(0, listOf(scoreName)) // Entities
                protocolManager.sendServerPacket(player, packet)

                packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreName) // Score Name
                packet.scoreboardActions.write(0, EnumWrappers.ScoreboardAction.CHANGE) // Action
                packet.strings.write(1, getPlayerIdentifier(player)) // Objective Name
                packet.integers.write(0, score) // Score Value
                protocolManager.sendServerPacket(player, packet)
            }

            playerBoard.scores.filter { !scores.containsKey(it.key) }.forEach { (score, _) ->
                val scoreName = scoreToName(score)

                var packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreName) // Team Name
                if (afterAquaticUpdate) {
                    packet.integers.write(0, 1) // Mode - remove team
                } else packet.integers.write(1, 1) // Mode - remove team
                protocolManager.sendServerPacket(player, packet)

                packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
                packet.modifier.writeDefaults()
                packet.strings.write(0, scoreName) // Score Name
                packet.scoreboardActions.write(0, EnumWrappers.ScoreboardAction.REMOVE) // Action
                packet.strings.write(1, getPlayerIdentifier(player)) // Objective Name
                protocolManager.sendServerPacket(player, packet)
            }

            playerBoard.also {
                it.title = title
                it.scores = scores
            }
        }
    }

    override fun hasScoreboard(player: Player): Boolean {
        return player.uniqueId in playerBoards
    }
}