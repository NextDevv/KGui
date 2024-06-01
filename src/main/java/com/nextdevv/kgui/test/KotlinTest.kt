package com.nextdevv.kgui.test

import com.nextdevv.kgui2.KGui
import com.nextdevv.kgui2.annotations.Experimental
import com.nextdevv.kgui2.models.Button
import com.nextdevv.kgui2.utils.KItemStack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.random.Random

class KotlinTest : JavaPlugin() {
    @OptIn(Experimental::class)
    fun test() {
        val flag = Random.nextBoolean()
        val kGui = KGui(this)
        kGui.createBuilder(Bukkit.getPlayer("NextDevv")!!)
            .item(0, KItemStack()
                .name("&cTest")
                .type(Material.DIAMOND)
                .build()
            )
            .button(1) { _: Player ->
                if(!flag) return@button null

                Button().onClick { player, builder ->
                    player.sendMessage("Clicked")
                }
            }
            .onInteract { event ->

            }
            .open()
    }
}