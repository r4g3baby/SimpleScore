package com.r4g3baby.simplescore.storage.providers.hikari

import com.r4g3baby.simplescore.configs.models.Storage
import com.r4g3baby.simplescore.storage.classloader.IsolatedClassLoader
import com.zaxxer.hikari.HikariConfig

class MySQLProvider(
    classLoader: IsolatedClassLoader, settings: Storage
) : HikariStorageProvider(classLoader, settings) {
    override val createTableQuery: String
        get() = """
            create table if not exists ${tablePrefix}players
            (
                uniqueId   varchar(36)  not null
                    primary key,
                hidden     tinyint(1)   not null,
                disabled   tinyint(1)   not null,
                scoreboard varchar(200) null
            ) default charset = utf8mb4
        """.trimIndent()

    override fun configureHikari(hikariConfig: HikariConfig, settings: Storage) {
        hikariConfig.driverClassName = "com.mysql.cj.jdbc.Driver"
        hikariConfig.jdbcUrl = "jdbc:mysql://${settings.address}/${settings.database}"
    }

    override fun setProperties(hikariConfig: HikariConfig, properties: MutableMap<String, Any>) {
        // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        properties.putIfAbsent("cachePrepStmts", true)
        properties.putIfAbsent("prepStmtCacheSize", 250)
        properties.putIfAbsent("prepStmtCacheSqlLimit", 2048)
        properties.putIfAbsent("useServerPrepStmts", true)
        properties.putIfAbsent("useLocalSessionState", true)
        properties.putIfAbsent("rewriteBatchedStatements", true)
        properties.putIfAbsent("cacheResultSetMetadata", true)
        properties.putIfAbsent("cacheServerConfiguration", true)
        properties.putIfAbsent("elideSetAutoCommits", true)
        properties.putIfAbsent("maintainTimeStats", false)
        properties.putIfAbsent("alwaysSendSetIsolation", false)
        properties.putIfAbsent("cacheCallableStmts", true)

        super.setProperties(hikariConfig, properties)
    }
}