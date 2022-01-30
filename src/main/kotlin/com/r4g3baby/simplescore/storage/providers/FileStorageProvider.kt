package com.r4g3baby.simplescore.storage.providers

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import java.nio.file.Path
import java.sql.Connection
import java.util.*

abstract class FileStorageProvider(val file: Path, val tableName: String) : StorageProvider {
    abstract val createTableQuery: String

    open val selectPlayerQuery: String
        get() = "SELECT hidden, disabled, scoreboard FROM ${tableName}_players WHERE uniqueId = ? LIMIT 1"
    open val insertPlayerQuery: String
        get() = "INSERT INTO ${tableName}_players(uniqueId, hidden, disabled, scoreboard) VALUES (?, ?, ?, ?)"
    open val updatePlayerQuery: String
        get() = "UPDATE ${tableName}_players SET hidden = ?, disabled = ?, scoreboard = ? WHERE uniqueId = ?"

    abstract fun createConnection(): Connection

    private lateinit var _connection: Connection
    override fun getConnection(): Connection {
        if (_connection.isClosed) {
            _connection = createConnection()
        }
        return _connection
    }

    override fun init() {
        _connection = createConnection()

        getConnection().let { conn ->
            conn.prepareStatement(createTableQuery).use { stmt ->
                stmt.execute()
            }
        }
    }

    override fun shutdown() {
        _connection.close()
    }

    override fun fetchPlayer(uniqueId: UUID): PlayerData? {
        getConnection().let { conn ->
            conn.prepareStatement(selectPlayerQuery).use { stmt ->
                stmt.setString(1, uniqueId.toString())

                val result = stmt.executeQuery()
                if (result.next()) {
                    return PlayerData().apply {
                        if (result.getBoolean("hidden")) {
                            hide(SimpleScore.plugin)
                        }

                        if (result.getBoolean("disabled")) {
                            disable(SimpleScore.plugin)
                        }

                        setScoreboard(SimpleScore.plugin, result.getString("scoreboard"))
                    }
                }
            }
        }
        return null
    }

    override fun createPlayer(uniqueId: UUID, playerData: PlayerData) {
        getConnection().let { conn ->
            conn.prepareStatement(insertPlayerQuery).use { stmt ->
                stmt.setString(1, uniqueId.toString())
                stmt.setBoolean(2, playerData.isHiding(SimpleScore.plugin))
                stmt.setBoolean(3, playerData.isDisabling(SimpleScore.plugin))
                stmt.setString(4, playerData.getScoreboard(SimpleScore.plugin))

                stmt.execute()
            }
        }
    }

    override fun savePlayer(uniqueId: UUID, playerData: PlayerData) {
        getConnection().let { conn ->
            conn.prepareStatement(updatePlayerQuery).use { stmt ->
                stmt.setBoolean(1, playerData.isHiding(SimpleScore.plugin))
                stmt.setBoolean(2, playerData.isDisabling(SimpleScore.plugin))
                stmt.setString(3, playerData.getScoreboard(SimpleScore.plugin))
                stmt.setString(4, uniqueId.toString())

                stmt.execute()
            }
        }
    }
}