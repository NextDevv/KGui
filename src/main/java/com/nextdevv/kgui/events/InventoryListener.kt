package com.nextdevv.kgui.events

import com.nextdevv.kgui.api.KGui
import com.nextdevv.kgui.api.id
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.AsyncPlayerChatEvent

class InventoryListener(private val kGui: KGui): Listener {
    @EventHandler
    fun onInventoryClickEvent(e: InventoryClickEvent) {
        val inv = e.clickedInventory ?: return
        if(inv.holder !is Player) return

        try {
            kGui.builders.keys.firstOrNull { it == (inv.holder as Player).uniqueId }?.let {
                val builders = kGui.builders[it]!!
                builders.forEach { builder ->
                    if (builder.builderNumber == inv.id)
                        e.isCancelled =
                            builder.onInventoryClick(inv.holder as Player, e.click, e.rawSlot, e.currentItem)
                }
            }
        }catch (_: Exception) {}
    }

    @EventHandler
    fun onInventoryCloseEvent(e: InventoryCloseEvent) {
        val inv = e.inventory
        if(inv.holder !is Player) return

        try {
            kGui.builders.keys.firstOrNull { it == (inv.holder as Player).uniqueId  }?.let {
                val builders = kGui.builders[it]!!
                builders.forEach { builder ->
                    builder.active = false
                    if(builder.builderNumber == inv.id)
                        builder.onInventoryClose(inv.holder as Player)
                }
            }
        }catch (_: Exception) {}
    }

    @EventHandler
    fun onInventoryOpenEvent(e: InventoryOpenEvent) {
        val inv = e.inventory
        if(inv.holder !is Player) return

        try {
            kGui.builders.keys.firstOrNull { it == (inv.holder as Player).uniqueId  }?.let {
                val builders = kGui.builders[it]!!
                builders.forEach { builder ->
                    builder.active = true
                    if(builder.builderNumber == inv.id) {
                        if(e.player.hasPermission(builder.getPermission() ?: "") || builder.getPermission().isNullOrEmpty())
                            builder.onInventoryOpen(inv.holder as Player)
                    }
                }
            }
        }catch (_: Exception) {}
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onPlayerChatEvent(e: AsyncPlayerChatEvent) {
        val player = e.player
        try {
            kGui.builders.keys.firstOrNull { it == player.uniqueId }?.let {
                val builders = kGui.builders[it]!!
                builders.forEach { builder ->
                    if(it in builder.waitingForPlayer) {
                        e.isCancelled = true
                        builder.waitingForPlayer.remove(it)
                        builder.responses[it] = e.message
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}