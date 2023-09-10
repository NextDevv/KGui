package com.nextdevv.kgui.models

import com.nextdevv.kgui.api.KGui
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GuiButton {
    private var itemStack: ItemStack? = null
    @get:JvmName("getOnClick1")
    @set:JvmName("setOnClick1")
    var onClick: ((KGui.Builder, Player) -> Unit)? = null
    var onRightClick: ((KGui.Builder, Player) -> Unit)? = null

    fun setItemStack(itemStack: ItemStack): GuiButton {
        this.itemStack = itemStack
        return this
    }

    fun setOnClick(onClick: (KGui.Builder, Player) -> Unit): GuiButton {
        this.onClick = onClick
        return this
    }

    fun onClick(onClick: (KGui.Builder, Player) -> Unit): GuiButton {
        this.onClick = onClick
        return this
    }

    fun setOnRightClick(onRightClick: (KGui.Builder, Player) -> Unit): GuiButton {
        this.onRightClick = onRightClick
        return this
    }

    fun onRightClick(onRightClick: (KGui.Builder, Player) -> Unit): GuiButton {
        this.onRightClick = onRightClick
        return this
    }

    fun getItemStack(): ItemStack? {
        return itemStack
    }

    fun getOnClick(): ((KGui.Builder, Player) -> Unit)? {
        return onClick
    }
}