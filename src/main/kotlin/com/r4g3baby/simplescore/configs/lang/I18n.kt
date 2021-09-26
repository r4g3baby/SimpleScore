package com.r4g3baby.simplescore.configs.lang

import org.bukkit.ChatColor.translateAlternateColorCodes
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.text.MessageFormat
import java.util.*
import java.util.logging.Level

class I18n(lang: String = "en", private val plugin: Plugin) {
    private lateinit var defaultBundle: ResourceBundle
    private lateinit var customBundle: ResourceBundle

    private val messageFormatCache = HashMap<String, MessageFormat>()
    private val nullBundle = object : ResourceBundle() {
        override fun getKeys(): Enumeration<String>? {
            return null
        }

        override fun handleGetObject(key: String): Any? {
            return null
        }
    }

    init {
        loadTranslations(lang)
    }

    fun t(key: String, vararg args: Any, prefixed: Boolean = true) = trans(key, *args, prefixed = prefixed)
    fun trans(key: String, vararg args: Any, prefixed: Boolean = true): String {
        val translation = translateAlternateColorCodes(
            '&', (if (prefixed) "${t("prefix", prefixed = false)} " else "") + try {
                try {
                    customBundle.getString(key)
                } catch (ex: MissingResourceException) {
                    defaultBundle.getString(key)
                }
            } catch (ex: MissingResourceException) {
                plugin.logger.log(Level.WARNING, "Missing translation key \"$key\".", ex)
                key
            }
        )

        if (args.isEmpty()) return translation
        return messageFormatCache.computeIfAbsent(translation) {
            MessageFormat(translation)
        }.format(args)
    }

    fun loadTranslations(lang: String) {
        ResourceBundle.clearCache()

        defaultBundle = try {
            ResourceBundle.getBundle("lang/messages", Locale(lang))
        } catch (ex: MissingResourceException) {
            nullBundle
        }

        customBundle = try {
            ResourceBundle.getBundle("messages", Locale(lang), PluginResClassLoader(plugin))
        } catch (ex: MissingResourceException) {
            nullBundle
        }
    }

    private class PluginResClassLoader(plugin: Plugin) : ClassLoader(plugin.javaClass.classLoader) {
        private val dataFolder = plugin.dataFolder

        override fun getResource(name: String): URL? {
            val file = File(dataFolder, name)
            if (file.exists()) {
                try {
                    return file.toURI().toURL()
                } catch (ex: MalformedURLException) {
                }
            }
            return null
        }

        override fun getResourceAsStream(name: String): InputStream? {
            val file = File(dataFolder, name)
            if (file.exists()) {
                try {
                    return FileInputStream(file)
                } catch (ex: FileNotFoundException) {
                }
            }
            return null
        }
    }
}
