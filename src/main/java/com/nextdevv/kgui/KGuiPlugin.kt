package com.nextdevv.kgui

import com.nextdevv.kgui.test.CommandTest
import org.bukkit.plugin.java.JavaPlugin

class KGuiPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: KGuiPlugin
    }

    init {
        instance = this
    }

    override fun onEnable() {
        // Plugin startup logic
        logger.info("=========== KGui ===========")
        logger.info("Author: NextDevv")
        logger.info("Version: 1.0")
        getCommand("kgui-test")?.setExecutor(CommandTest());
        logger.info("=========== KGui ===========")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
