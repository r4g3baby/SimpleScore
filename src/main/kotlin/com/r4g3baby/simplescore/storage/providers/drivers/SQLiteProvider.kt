package com.r4g3baby.simplescore.storage.providers.drivers

import com.r4g3baby.simplescore.storage.classloader.IsolatedClassLoader
import com.r4g3baby.simplescore.storage.providers.FileStorageProvider
import java.lang.reflect.Constructor
import java.nio.file.Path
import java.sql.Connection
import java.sql.SQLException
import java.util.*

class SQLiteProvider(
    file: Path, classLoader: IsolatedClassLoader, tableName: String
) : FileStorageProvider(file, tableName) {
    override val createTableQuery: String
        get() = """
            create table if not exists ${tableName}_players
            (
                uniqueId   VARCHAR(36) not null
                    constraint ${tableName}_players_pk
                        primary key,
                hidden     BOOL        not null,
                disabled   BOOL        not null,
                scoreboard VARCHAR(200)
            )
        """.trimIndent()

    private val connectionConstructor: Constructor<*>

    init {
        try {
            val connectionClass = classLoader.loadClass("org.sqlite.jdbc4.JDBC4Connection")
            connectionConstructor = connectionClass.getConstructor(
                String::class.java, String::class.java, Properties::class.java
            )
        } catch (ex: ReflectiveOperationException) {
            throw RuntimeException(ex)
        }
    }

    override fun createConnection(): Connection {
        try {
            return connectionConstructor.newInstance("jdbc:sqlite:$file", file.toString(), Properties()) as Connection
        } catch (ex: ReflectiveOperationException) {
            if (ex.cause is SQLException) {
                throw ex.cause as SQLException
            }
            throw RuntimeException(ex)
        }
    }
}