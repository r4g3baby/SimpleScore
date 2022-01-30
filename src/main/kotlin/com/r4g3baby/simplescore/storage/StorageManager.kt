package com.r4g3baby.simplescore.storage

import com.google.common.io.ByteStreams
import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.scoreboard.models.PlayerData
import com.r4g3baby.simplescore.storage.classloader.IsolatedClassLoader
import com.r4g3baby.simplescore.storage.models.Driver
import com.r4g3baby.simplescore.storage.providers.StorageProvider
import com.r4g3baby.simplescore.storage.providers.drivers.H2Provider
import com.r4g3baby.simplescore.storage.providers.drivers.SQLiteProvider
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class StorageManager {
    private var provider: StorageProvider? = null

    init {
        val driver = SimpleScore.config.storageDriver
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
            val tableName = "simplescore"

            provider = when (driver) {
                Driver.H2 -> H2Provider(
                    pluginDataFolder.resolve("data-$driverName"), classLoader, tableName
                )
                Driver.SQLite -> SQLiteProvider(
                    pluginDataFolder.resolve("data-$driverName.db"), classLoader, tableName
                )
            }.apply { init() }
        }
    }

    fun shutdown() {
        provider?.shutdown()
    }

    fun fetchPlayer(uniqueId: UUID): PlayerData? {
        return provider?.fetchPlayer(uniqueId)
    }

    fun createPlayer(uniqueId: UUID, playerData: PlayerData) {
        provider?.createPlayer(uniqueId, playerData)
    }

    fun savePlayer(uniqueId: UUID, playerData: PlayerData) {
        provider?.savePlayer(uniqueId, playerData)
    }

    private fun downloadDriver(driver: Driver, driverFile: Path) {
        val connection = URL("https://repo1.maven.org/maven2/${driver.mavenPath}").openConnection()
        connection.getInputStream().use { inputStream ->
            val bytes = ByteStreams.toByteArray(inputStream)
            check(bytes.isNotEmpty()) { "Empty stream" }
            Files.write(driverFile, bytes)
        }
    }
}