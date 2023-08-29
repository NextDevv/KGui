package com.nextdevv.kgui.test

import com.nextdevv.kgui.KGuiPlugin
import com.nextdevv.kgui.api.KGui
import com.nextdevv.kgui.item.KItemStack
import com.nextdevv.kgui.models.GuiBorder
import com.nextdevv.kgui.models.GuiButton
import com.nextdevv.kgui.models.Pages
import com.nextdevv.kgui.utils.Alignment
import com.nextdevv.kgui.utils.randomMaterial
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class CommandTest : CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if(!p0.isOp) {
            p0.sendMessage("You must be an operator to use this command!")
            return false
        }

        val border = GuiBorder()
        border.defaultItemStack = ItemStack(Material.RED_STAINED_GLASS_PANE)
        val inv = KGui(KGuiPlugin.instance).builder()
            .setTitle("&cItem Search")
            .setRows(6)
            .setBorder(border)
            .addButton(Alignment.BOTTOM_CENTER, GuiButton().setItemStack(
                KItemStack().builder()
                    .setName("&c&lSearch")
                    .setMaterial(Material.COMPASS)
                    .build()
            ).setOnClick { builder, player ->
                builder.askForInput(player, "&cEnter search query:").thenAccept { input ->
                    try {
                        val list = Material.entries.filter { it.name.lowercase().contains(input.lowercase()) }.map { KItemStack().builder().setMaterial(it).build() }
                        val pages = Pages().builder()
                            .setMaxPerPages(27)
                            .autoCreatePages(list)
                            .build()
                        val new = builder
                            .createCacheVariable("search", input)
                            .createCacheVariable("size", pages.pages.size)
                            .setTitle("&fSearch Results for &8[&c$input&8] &8[&fPage &c1&8/&c${pages.pages.size}&8] ")
                            .setPages(pages)
                            .build()
                        object : BukkitRunnable() {
                            override fun run() {
                                player.openInventory(new)
                            }
                        }.runTask(JavaPlugin.getPlugin(KGuiPlugin::class.java))
                    }catch (e: Exception) {
                        println("ERROR")
                        e.printStackTrace()
                    }
                }
            })
            .addItemStackClickListener { itemStack, player, builder ->
                player.inventory.addItem(itemStack)
            }
            .addButton(Alignment.BOTTOM_LEFT, GuiButton().setItemStack(
                KItemStack().builder()
                    .setName("&cPrevious Page")
                    .setMaterial(Material.ARROW)
                    .build()
            ).setOnClick { builder, player ->
                val currentPage = builder.currentPage
                if(currentPage == 1) {
                    return@setOnClick
                }
                val input = builder.getCacheVariable<String>("search")
                val size = builder.getCacheVariable<Int>("size")
                val new = builder
                    .setTitle("&fSearch Results for &8[&c$input&8] &8[&fPage &c${currentPage-1}&8/&c${size}&8] ")
                    .setCurrentPage(currentPage - 1)
                    .build()
                player.openInventory(new)
            })
            .addButton(Alignment.BOTTOM_RIGHT, GuiButton().setItemStack(
                KItemStack().builder()
                    .setName("&cNext Page")
                    .setMaterial(Material.ARROW)
                    .build()
            ).setOnClick { builder, player ->
                val currentPage = builder.currentPage
                val input = builder.getCacheVariable<String>("search")
                val size = builder.getCacheVariable<Int>("size")
                if(currentPage == size) {
                    return@setOnClick
                }
                val new = builder
                    .setTitle("&fSearch Results for &8[&c$input&8] &8[&fPage &c${currentPage+1}&8/&c${size}&8] ")
                    .setCurrentPage(currentPage + 1)
                    .build()
                player.openInventory(new)
            })
            .build()
        if(p0 is Player) {
            p0.openInventory(inv)
        }else {
            p0.sendMessage("You must be a player to use this command!")
        }
        return true
    }
}