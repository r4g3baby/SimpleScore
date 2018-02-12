package com.r4g3baby.simplescore.utils.updater

import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.function.Consumer

class UpdateChecker(plugin: JavaPlugin, pluginID: Int, consumer: Consumer<String?>) {
    private val _spigetApi = "https://api.spiget.org/v2/resources/%s/versions?sort=-name"
    private val _spigotUrl = "https://www.spigotmc.org/resources/%s"

    init {
        plugin.server.scheduler.runTaskAsynchronously(plugin, {
            try {
                val conn = URL(_spigetApi.format(pluginID)).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.useCaches = false
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "${plugin.name}/${plugin.description.version}")

                val versions = JSONValue.parseWithException(BufferedReader(InputStreamReader(conn.inputStream))) as JSONArray
                val latestVersion = ((versions[0] as JSONObject)["name"] as String).replace(".", "")
                val currentVersion = plugin.description.version.replace(".", "")

                if (currentVersion.toInt() < latestVersion.toInt()) {
                    consumer.accept(_spigotUrl.format(pluginID))
                }
                conn.disconnect()
            } catch (ignored: Exception) {
            }
        })
    }
}