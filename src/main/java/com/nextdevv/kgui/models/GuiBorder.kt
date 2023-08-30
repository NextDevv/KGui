package com.nextdevv.kgui.models

import com.nextdevv.kgui.item.KItemStack
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

data class GuiBorder(
        var leftOffset: Int = 0,
        var rightOffset: Int = 0,
        var topOffset: Int = 0,
        var bottomOffset: Int = 0,

        var leftItemStack: ItemStack? = null,
        var rightItemStack: ItemStack? = null,
        var topItemStack: ItemStack? = null,
        var bottomItemStack: ItemStack? = null,

        var defaultItemStack: ItemStack = KItemStack().builder()
                .setName(" ")
                .addItemFlag(ItemFlag.HIDE_UNBREAKABLE)
                .setMaterial(Material.STONE)
                .build()
)
