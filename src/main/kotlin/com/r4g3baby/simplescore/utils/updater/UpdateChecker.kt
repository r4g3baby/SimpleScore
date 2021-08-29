package com.r4g3baby.simplescore.utils.updater

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.swiftzer.semver.SemVer
import org.bukkit.plugin.Plugin
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.function.BiConsumer

class UpdateChecker(plugin: Plugin, pluginId: Int, consumer: BiConsumer<Boolean, String>) {
    private val _spigotApi = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=$pluginId"

    init {
        plugin.server.scheduler.runTaskAsynchronously(plugin) {
            try {
                val conn = URL(_spigotApi).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.useCaches = false
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "${plugin.name}/${plugin.description.version}")

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonObject = Gson().fromJson(reader, JsonObject::class.java)
                val latestVersion = SemVer.parse(jsonObject.get("current_version").asString)
                val currentVersion = SemVer.parse(plugin.description.version)

                if (currentVersion < latestVersion) {
                    consumer.accept(true, latestVersion.toString())
                } else consumer.accept(false, latestVersion.toString())

                reader.close()
                conn.disconnect()
            } catch (ignored: Exception) {
                consumer.accept(false, "")
            }
        }
    }
}