package com.r4g3baby.simplescore.storage.providers

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import com.r4g3baby.simplescore.storage.models.Storage
import java.sql.Connection
import java.util.*

abstract class SQLStorageProvider(val settings: Storage) : StorageProvider {
    protected val tablePrefix: String = settings.tablePrefix

    abstract val createTableQuery: String

    open val selectPlayerQuery: String =
        "SELECT hidden, disabled, scoreboard FROM ${tablePrefix}players WHERE uniqueId = ? LIMIT 1"

    open val insertPlayerQuery: String =
        "INSERT INTO ${tablePrefix}players(uniqueId, hidden, disabled, scoreboard) VALUES (?, ?, ?, ?)"

    open val updatePlayerQuery: String =
        "UPDATE ${tablePrefix}players SET hidden = ?, disabled = ?, scoreboard = ? WHERE uniqueId = ?"

    abstract fun <R> withConnection(action: (Connection) -> R): R

    override fun init() {
        withConnection { conn ->
            conn.prepareStatement(createTableQuery).use { stmt ->
                stmt.execute()
            }
        }
    }

    override fun fetchPlayer(uniqueId: UUID): PlayerData? {
        return withConnection { conn ->
            conn.prepareStatement(selectPlayerQuery).use { stmt ->
                stmt.setString(1, uniqueId.toString())

                val result = stmt.executeQuery()
                if (result.next()) {
                    return@withConnection PlayerData(uniqueId).apply {
                        if (result.getBoolean("hidden")) {
                            hide(SimpleScore.plugin)
                        }

                        if (result.getBoolean("disabled")) {
                            disable(SimpleScore.plugin)
                        }

                        setScoreboard(SimpleScore.plugin, result.getString("scoreboard"))
                    }
                }
                return@withConnection null
            }
        }
    }

    override fun createPlayer(playerData: PlayerData) {
        withConnection { conn ->
            conn.prepareStatement(insertPlayerQuery).use { stmt ->
                stmt.setString(1, playerData.uniqueId.toString())
                stmt.setBoolean(2, playerData.isHiding(SimpleScore.plugin))
                stmt.setBoolean(3, playerData.isDisabling(SimpleScore.plugin))
                stmt.setString(4, playerData.getScoreboard(SimpleScore.plugin))

                stmt.execute()
            }
        }
    }

    override fun savePlayer(playerData: PlayerData) {
        withConnection { conn ->
            conn.prepareStatement(updatePlayerQuery).use { stmt ->
                stmt.setBoolean(1, playerData.isHiding(SimpleScore.plugin))
                stmt.setBoolean(2, playerData.isDisabling(SimpleScore.plugin))
                stmt.setString(3, playerData.getScoreboard(SimpleScore.plugin))
                stmt.setString(4, playerData.uniqueId.toString())

                stmt.execute()
            }
        }
    }
}