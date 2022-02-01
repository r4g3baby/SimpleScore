package com.r4g3baby.simplescore.storage.providers

import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import java.util.*

interface StorageProvider {
    fun init()
    fun shutdown()

    fun fetchPlayer(uniqueId: UUID): PlayerData?
    fun createPlayer(uniqueId: UUID, playerData: PlayerData)
    fun savePlayer(uniqueId: UUID, playerData: PlayerData)
}