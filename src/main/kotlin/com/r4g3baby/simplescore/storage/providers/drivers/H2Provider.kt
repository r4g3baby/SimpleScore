package com.r4g3baby.simplescore.storage.providers.drivers

import com.r4g3baby.simplescore.storage.classloader.IsolatedClassLoader
import com.r4g3baby.simplescore.storage.providers.FileStorageProvider
import java.lang.reflect.Constructor
import java.nio.file.Path
import java.sql.Connection
import java.sql.SQLException
import java.util.*

class H2Provider(
    file: Path, classLoader: IsolatedClassLoader, tableName: String
) : FileStorageProvider(file, tableName) {
    override val createTableQuery: String
        get() = """
            create table if not exists ${tableName}_players
            (
                uniqueId   VARCHAR(36) not null,
                hidden     BOOL        not null,
                disabled   BOOL        not null,
                scoreboard VARCHAR(200),
                constraint ${tableName}_players_pk
                    primary key (uniqueId)
            )
        """.trimIndent()

    private val connectionConstructor: Constructor<*>

    init {
        try {
            val connectionClass = classLoader.loadClass("org.h2.jdbc.JdbcConnection")
            connectionConstructor = connectionClass.getConstructor(
                String::class.java, Properties::class.java
            )
        } catch (ex: ReflectiveOperationException) {
            throw RuntimeException(ex)
        }
    }

    override fun createConnection(): Connection {
        try {
            return connectionConstructor.newInstance("jdbc:h2:$file", Properties()) as Connection
        } catch (ex: ReflectiveOperationException) {
            if (ex.cause is SQLException) {
                throw ex.cause as SQLException
            }
            throw RuntimeException(ex)
        }
    }
}