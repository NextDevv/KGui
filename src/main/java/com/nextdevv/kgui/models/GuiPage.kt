package com.nextdevv.kgui.models

import org.bukkit.inventory.ItemStack

class GuiPage {
    private val content: MutableList<ItemStack> = mutableListOf()

    fun addItem(itemStack: ItemStack): GuiPage {
        content.add(itemStack)
        return this
    }

    fun addItem(itemStack: ItemStack, index: Int): GuiPage {
        content.add(index, itemStack)
        return this
    }

    fun removeItem(index: Int): GuiPage {
        content.removeAt(index)
        return this
    }

    fun removeItem(itemStack: ItemStack): GuiPage {
        content.remove(itemStack)
        return this
    }

    fun filterContent(predicate: (ItemStack) -> Boolean): GuiPage {
        content.filter(predicate)
        return this
    }

    fun getContent(): MutableList<ItemStack> {
        return content
    }
}
