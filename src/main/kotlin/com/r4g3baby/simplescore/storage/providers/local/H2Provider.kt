package com.r4g3baby.simplescore.storage.providers.local

import com.r4g3baby.simplescore.storage.classloader.IsolatedClassLoader
import com.r4g3baby.simplescore.storage.models.Storage
import java.lang.reflect.Constructor
import java.nio.file.Path
import java.sql.Connection
import java.sql.SQLException
import java.util.*

class H2Provider(
    classLoader: IsolatedClassLoader, file: Path, settings: Storage
) : LocalStorageProvider(file, settings) {
    override val createTableQuery: String = """
        create table if not exists ${tablePrefix}players
        (
            uniqueId   VARCHAR(36) not null,
            hidden     BOOL        not null,
            disabled   BOOL        not null,
            scoreboard VARCHAR(200),
            constraint ${tablePrefix}players_pk
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