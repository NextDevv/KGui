package com.nextdevv.kgui2.models

import com.nextdevv.kgui.utils.tac
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class Border {
    var leftOffset: Int = 0
    var rightOffset: Int = 0
    var topOffset: Int = 0
    var bottomOffset: Int = 0

    var leftItemStack: ItemStack? = null
    var rightItemStack: ItemStack? = null
    var topItemStack: ItemStack? = null
    var bottomItemStack: ItemStack? = null

    var defaultItemStack: ItemStack = ItemStack(Material.GLASS_PANE).apply {
        itemMeta = itemMeta?.apply {
            addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
            setDisplayName("&0".tac())
        }
    }
}