package com.r4g3baby.simplescore.utils.updater

import org.bukkit.plugin.Plugin
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.function.Consumer

class UpdateChecker(plugin: Plugin, pluginID: Int, consumer: Consumer<String>) {
    private val _spigotApi = "https://api.spigotmc.org/legacy/update.php?resource=%s"
    private val _spigotUrl = "https://www.spigotmc.org/resources/%s"

    init {
        plugin.server.scheduler.runTaskAsynchronously(plugin) {
            try {
                val conn = URL(_spigotApi.format(pluginID)).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.useCaches = false
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "${plugin.name}/${plugin.description.version}")

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val latestVersion = reader.readText().replace(".", "")
                val currentVersion = plugin.description.version.replace(".", "")

                if (currentVersion.toInt() < latestVersion.toInt()) {
                    consumer.accept(_spigotUrl.format(pluginID))
                }

                reader.close()
                conn.disconnect()
            } catch (ignored: Exception) {
            }
        }
    }
}