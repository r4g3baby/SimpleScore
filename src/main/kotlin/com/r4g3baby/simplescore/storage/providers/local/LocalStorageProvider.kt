package com.r4g3baby.simplescore.storage.providers.local

import com.r4g3baby.simplescore.storage.models.Storage
import com.r4g3baby.simplescore.storage.providers.SQLStorageProvider
import java.nio.file.Path
import java.sql.Connection

abstract class LocalStorageProvider(
    val file: Path, settings: Storage
) : SQLStorageProvider(settings) {
    abstract fun createConnection(): Connection

    private lateinit var connection: Connection
    private fun getConnection(): Connection {
        if (connection.isClosed) {
            connection = createConnection()
        }
        return connection
    }

    override fun <R> withConnection(action: (Connection) -> R): R {
        return getConnection().let(action)
    }

    override fun init() {
        connection = createConnection()

        super.init()
    }

    override fun shutdown() {
        connection.close()
    }
}