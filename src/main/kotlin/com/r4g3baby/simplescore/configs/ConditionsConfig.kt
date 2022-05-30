package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.scoreboard.models.Condition
import com.r4g3baby.simplescore.scoreboard.models.conditions.*
import com.r4g3baby.simplescore.utils.configs.ConfigFile
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin

class ConditionsConfig(plugin: Plugin) : ConfigFile(plugin, "conditions"), Iterable<Map.Entry<String, Condition>> {
    private val conditions = HashMap<String, Condition>()

    init {
        config.getKeys(false).filter { !conditions.containsKey(it.lowercase()) }.forEach { condition ->
            if (!config.isConfigurationSection(condition)) return@forEach

            val conditionSec = config.getConfigurationSection(condition)
            when (conditionSec.getConditionType("type")) {
                Condition.Type.HAS_PERMISSION -> {
                    conditions[condition.lowercase()] = HasPermission(
                        conditionSec.getString("permission")
                    )
                }
                Condition.Type.GREATER_THAN -> {
                    conditions[condition.lowercase()] = GreaterThan(
                        conditionSec.getString("input"),
                        conditionSec.getString("value"),
                        conditionSec.getBoolean("orEqual", false)
                    )
                }
                Condition.Type.LESS_THAN -> {
                    conditions[condition.lowercase()] = LessThan(
                        conditionSec.getString("input"),
                        conditionSec.getString("value"),
                        conditionSec.getBoolean("orEqual", false)
                    )
                }
                Condition.Type.EQUALS -> {
                    conditions[condition.lowercase()] = Equals(
                        conditionSec.getString("input"),
                        conditionSec.getString("value"),
                        conditionSec.getBoolean("ignoreCase", false)
                    )
                }
                Condition.Type.CONTAINS -> {
                    conditions[condition.lowercase()] = Contains(
                        conditionSec.getString("input"),
                        conditionSec.getString("value"),
                        conditionSec.getBoolean("ignoreCase", false)
                    )
                }
                Condition.Type.ENDS_WITH -> {
                    conditions[condition.lowercase()] = EndsWith(
                        conditionSec.getString("input"),
                        conditionSec.getString("value"),
                        conditionSec.getBoolean("ignoreCase", false)
                    )
                }
                Condition.Type.STARTS_WITH -> {
                    conditions[condition.lowercase()] = StartsWith(
                        conditionSec.getString("input"),
                        conditionSec.getString("value"),
                        conditionSec.getBoolean("ignoreCase", false)
                    )
                }
                else -> {
                    val typeValue = conditionSec.get("type")
                    plugin.logger.warning(
                        "Invalid type value for conditon: $condition, value: $typeValue."
                    )
                }
            }
        }
    }

    val keys: Set<String> get() = conditions.keys
    val values: Collection<Condition> get() = conditions.values

    operator fun get(condition: String): Condition? {
        return conditions[condition.lowercase()]
    }

    override fun iterator(): Iterator<Map.Entry<String, Condition>> {
        return conditions.iterator()
    }

    private fun ConfigurationSection.getConditionType(path: String): Condition.Type? {
        if (!contains(path)) return null
        return Condition.Type.fromValue(getString(path))
    }
}