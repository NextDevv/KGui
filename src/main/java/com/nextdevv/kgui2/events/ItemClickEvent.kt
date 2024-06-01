package com.nextdevv.kgui2.events

import com.nextdevv.kgui2.KGui.Builder
import com.nextdevv.kgui2.annotations.Experimental
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

data class ItemClickEvent @OptIn(Experimental::class) constructor(
        val player: Player,
        val item: ItemStack,
        val rawSlot: Int,
        val clickType: ClickType,
        val gui: Builder
) {
}