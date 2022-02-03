package com.r4g3baby.simplescore.storage.providers.hikari

import com.r4g3baby.simplescore.configs.models.Storage
import com.r4g3baby.simplescore.storage.classloader.IsolatedClassLoader
import com.zaxxer.hikari.HikariConfig

class MariaDBProvider(
    classLoader: IsolatedClassLoader, settings: Storage, tableName: String
) : HikariStorageProvider(classLoader, settings, tableName) {
    override val createTableQuery: String
        get() = """
            create table if not exists ${tableName}_players
            (
                uniqueId   varchar(36)  not null
                    primary key,
                hidden     tinyint(1)   not null,
                disabled   tinyint(1)   not null,
                scoreboard varchar(200) null
            ) default charset = utf8mb4
        """.trimIndent()

    override fun configureHikari(hikariConfig: HikariConfig, settings: Storage) {
        hikariConfig.driverClassName = "org.mariadb.jdbc.Driver"
        hikariConfig.jdbcUrl = "jdbc:mariadb://${settings.address}/${settings.database}"
    }
}