package com.nextdevv.kgui.events

import com.nextdevv.kgui.api.KGui
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerChatEvent

class InventoryListener(private val kGui: KGui): Listener {
    @EventHandler
    fun onInventoryClickEvent(e: InventoryClickEvent) {
        val inv = e.clickedInventory ?: return
        if(inv.holder !is Player) return

        kGui.builders.keys.firstOrNull { it == (inv.holder as Player).uniqueId  }?.let {
            val builder = kGui.builders[it]!!
            e.isCancelled = builder.onInventoryClick(inv.holder as Player, e.click, e.rawSlot, e.currentItem)
        }
    }

    @EventHandler
    fun onInventoryCloseEvent(e: InventoryClickEvent) {
        val inv = e.inventory
        if(inv.holder !is Player) return

        kGui.builders.keys.firstOrNull { it == (inv.holder as Player).uniqueId  }?.let {
            val builder = kGui.builders[it]!!
            builder.onInventoryClose(inv.holder as Player)
        }
    }

    @EventHandler
    fun onInventoryOpenEvent(e: InventoryClickEvent) {
        val inv = e.inventory
        if(inv.holder !is Player) return

        kGui.builders.keys.firstOrNull { it == (inv.holder as Player).uniqueId  }?.let {
            val builder = kGui.builders[it]!!
            builder.onInventoryOpen(inv.holder as Player)
        }
    }

    @EventHandler
    fun onPlayerChatEvent(e: AsyncPlayerChatEvent) {
        val player = e.player
        kGui.builders.keys.firstOrNull { it == player.uniqueId }?.let {
            val builder = kGui.builders[it]!!
            if(it in builder.waitingForPlayer) {
                e.isCancelled = true
                builder.waitingForPlayer.remove(it)
                builder.responses[it] = e.message
            }
        }
    }
}