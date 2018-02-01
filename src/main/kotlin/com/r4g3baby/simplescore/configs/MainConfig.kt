package com.r4g3baby.simplescore.configs

import com.r4g3baby.simplescore.SimpleScore
import com.r4g3baby.simplescore.utils.configs.ConfigFile

class MainConfig(plugin: SimpleScore) : ConfigFile(plugin, "config") {
    val updateTime: Int = config.getInt("UpdateTime", 30)
}