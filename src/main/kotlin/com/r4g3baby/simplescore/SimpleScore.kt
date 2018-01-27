package com.r4g3baby.simplescore

import org.bukkit.plugin.java.JavaPlugin

class SimpleScore: JavaPlugin() {

    override fun onEnable() {
        println("Enabling")
    }

    override fun onDisable() {
        println("Disabling")
    }
}