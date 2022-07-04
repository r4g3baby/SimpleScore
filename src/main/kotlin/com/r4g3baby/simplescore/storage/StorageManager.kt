package com.r4g3baby.simplescore.storage

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import com.r4g3baby.simplescore.storage.classloader.IsolatedClassLoader
import com.r4g3baby.simplescore.storage.models.Driver
import com.r4g3baby.simplescore.storage.providers.StorageProvider
import com.r4g3baby.simplescore.storage.providers.hikari.MariaDBProvider
import com.r4g3baby.simplescore.storage.providers.hikari.MySQLProvider
import com.r4g3baby.simplescore.storage.providers.hikari.PostgreSQLProvider
import com.r4g3baby.simplescore.storage.providers.local.H2Provider
import com.r4g3baby.simplescore.storage.providers.local.SQLiteProvider
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*

class StorageManager {
    private val sha256 = MessageDigest.getInstance("SHA-256")
    private var provider: StorageProvider? = null

    init {
        val driver = SimpleScore.config.storage.driver
        if (driver != null) {
            val pluginDataFolder = SimpleScore.plugin.dataFolder.toPath().toAbsolutePath()
            if (!Files.exists(pluginDataFolder)) {
                Files.createDirectories(pluginDataFolder)
            }

            val driversFolder = pluginDataFolder.resolve("drivers")
            if (!Files.exists(driversFolder)) {
                Files.createDirectories(driversFolder)
            }

            val driverFile = driversFolder.resolve(driver.fileName)
            if (!Files.exists(driverFile)) {
                downloadDriver(driver, driverFile)
            }

            val urls = arrayOf(driverFile.toUri().toURL())
            val classLoader = IsolatedClassLoader(urls)

            val driverName = driver.name.lowercase()
            val storageSettings = SimpleScore.config.storage

            provider = when (driver) {
                Driver.H2 -> H2Provider(
                    classLoader, pluginDataFolder.resolve("data-$driverName"), storageSettings
                )
                Driver.SQLite -> SQLiteProvider(
                    classLoader, pluginDataFolder.resolve("data-$driverName.db"), storageSettings
                )
                Driver.PostgreSQL -> PostgreSQLProvider(
                    classLoader, storageSettings
                )
                Driver.MariaDB -> MariaDBProvider(
                    classLoader, storageSettings
                )
                Driver.MySQL -> MySQLProvider(
                    classLoader, storageSettings
                )
            }.apply { init() }
        }
    }

    internal fun shutdown() {
        provider?.shutdown()
    }

    fun fetchPlayer(uniqueId: UUID): PlayerData? {
        return provider?.fetchPlayer(uniqueId)
    }

    fun createPlayer(playerData: PlayerData) {
        provider?.createPlayer(playerData)
    }

    fun savePlayer(playerData: PlayerData) {
        provider?.savePlayer(playerData)
    }

    private fun downloadDriver(driver: Driver, driverFile: Path) {
        val connection = URL("https://repo1.maven.org/maven2/${driver.mavenPath}").openConnection()
        connection.getInputStream().use { inputStream ->
            val bytes = inputStream.readBytes()
            check(bytes.isNotEmpty()) { "Empty stream" }
            check(driver.validateHash(sha256.digest(bytes))) { "Invalid hash" }
            Files.write(driverFile, bytes)
        }
    }
}