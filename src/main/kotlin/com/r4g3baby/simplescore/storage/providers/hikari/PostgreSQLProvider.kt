package com.r4g3baby.simplescore.storage.providers.hikari

import com.r4g3baby.simplescore.configs.models.Storage
import com.r4g3baby.simplescore.storage.classloader.IsolatedClassLoader
import com.zaxxer.hikari.HikariConfig

class PostgreSQLProvider(
    classLoader: IsolatedClassLoader, settings: Storage
) : HikariStorageProvider(classLoader, settings) {
    override val createTableQuery: String
        get() = """
            create table if not exists ${tablePrefix}players
            (
                "uniqueId" varchar(36) not null
                    constraint ${tablePrefix}players_pk
                        primary key,
                hidden     boolean     not null,
                disabled   boolean     not null,
                scoreboard varchar(200)
            )
        """.trimIndent()

    override val selectPlayerQuery: String
        get() = "SELECT hidden, disabled, scoreboard FROM ${tablePrefix}players WHERE \"uniqueId\" = ? LIMIT 1"
    override val insertPlayerQuery: String
        get() = "INSERT INTO ${tablePrefix}players(\"uniqueId\", hidden, disabled, scoreboard) VALUES (?, ?, ?, ?)"
    override val updatePlayerQuery: String
        get() = "UPDATE ${tablePrefix}players SET hidden = ?, disabled = ?, scoreboard = ? WHERE \"uniqueId\" = ?"

    override fun configureHikari(hikariConfig: HikariConfig, settings: Storage) {
        hikariConfig.driverClassName = "org.postgresql.Driver"
        hikariConfig.jdbcUrl = "jdbc:postgresql://${settings.address}/${settings.database}"
    }
}