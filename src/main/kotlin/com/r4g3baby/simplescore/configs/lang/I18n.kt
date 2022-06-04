package com.r4g3baby.simplescore.configs.lang

import com.r4g3baby.simplescore.utils.translateHexColorCodes
import org.bukkit.ChatColor.translateAlternateColorCodes
import org.bukkit.plugin.Plugin
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.text.MessageFormat
import java.util.*
import java.util.logging.Level

class I18n(locale: String = "en", private val plugin: Plugin) {
    private var currentLocale = Locale.getDefault()
    private lateinit var defaultBundle: ResourceBundle
    private lateinit var customBundle: ResourceBundle

    private val messageFormatCache = HashMap<String, MessageFormat>()

    init {
        loadTranslations(locale)
    }

    fun t(key: String, vararg args: Any, prefixed: Boolean = true) = trans(key, *args, prefixed = prefixed)
    fun trans(key: String, vararg args: Any, prefixed: Boolean = true): String {
        val translation = translateHexColorCodes(
            translateAlternateColorCodes(
                '&', (if (prefixed) "${t("prefix", prefixed = false)} " else "") + try {
                    try {
                        customBundle.getString(key)
                    } catch (_: MissingResourceException) {
                        defaultBundle.getString(key)
                    }
                } catch (ex: MissingResourceException) {
                    plugin.logger.log(Level.WARNING, "Missing translation key \"$key\".", ex)
                    key
                }
            )
        )

        if (args.isEmpty()) return translation
        return messageFormatCache.computeIfAbsent(translation) {
            MessageFormat(translation)
        }.format(args)
    }

    fun loadTranslations(locale: String) {
        val parts = locale.split("_", "-", ".", "\\", "/")
        currentLocale = when (parts.size) {
            1 -> Locale(parts[0])
            2 -> Locale(parts[0], parts[1])
            3 -> Locale(parts[0], parts[1], parts[2])
            else -> Locale(locale)
        }

        ResourceBundle.clearCache()

        defaultBundle = try {
            ResourceBundle.getBundle("lang/messages", currentLocale, UTF8PropertiesControl)
        } catch (_: MissingResourceException) {
            NullBundle
        }

        customBundle = try {
            ResourceBundle.getBundle("messages", currentLocale, PluginResClassLoader(plugin), UTF8PropertiesControl)
        } catch (_: MissingResourceException) {
            NullBundle
        }
    }

    private class PluginResClassLoader(plugin: Plugin) : ClassLoader(plugin.javaClass.classLoader) {
        private val dataFolder = plugin.dataFolder

        override fun getResource(name: String): URL? {
            val file = File(dataFolder, name)
            if (file.exists()) {
                try {
                    return file.toURI().toURL()
                } catch (_: MalformedURLException) {
                }
            }
            return null
        }

        override fun getResourceAsStream(name: String): InputStream? {
            val file = File(dataFolder, name)
            if (file.exists()) {
                try {
                    return FileInputStream(file)
                } catch (_: FileNotFoundException) {
                }
            }
            return null
        }
    }

    private object UTF8PropertiesControl : ResourceBundle.Control() {
        override fun newBundle(baseName: String?, locale: Locale?, format: String?, loader: ClassLoader?, reload: Boolean): ResourceBundle? {
            val resourceName = toResourceName(toBundleName(baseName, locale), "properties")

            var stream: InputStream? = null
            if (reload) {
                val url = loader?.getResource(resourceName)
                if (url != null) {
                    val connection = url.openConnection()
                    if (connection != null) {
                        connection.useCaches = false
                        stream = connection.getInputStream()
                    }
                }
            } else stream = loader?.getResourceAsStream(resourceName)

            stream?.use {
                return PropertyResourceBundle(InputStreamReader(it, Charsets.UTF_8))
            }
            return null
        }
    }

    private object NullBundle : ResourceBundle() {
        override fun handleGetObject(key: String): Any? {
            return null
        }

        override fun getKeys(): Enumeration<String>? {
            return null
        }

        override fun toString(): String {
            return "NullBundle"
        }
    }
}
