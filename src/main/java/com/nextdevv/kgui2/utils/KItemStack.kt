package com.nextdevv.kgui2.utils

import com.nextdevv.kgui.utils.tac
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class KItemStack(private val itemStack: ItemStack = ItemStack(Material.AIR)) {
    fun lore(lore: List<String>): KItemStack {
        val meta = itemStack.itemMeta
        meta?.lore = lore.map { it.tac() }
        itemStack.itemMeta = meta
        return this
    }

    fun lore(vararg lore: String): KItemStack {
        return lore(lore.toList())
    }

    fun name(name: String): KItemStack {
        val meta = itemStack.itemMeta
        meta?.setDisplayName(name.tac())
        itemStack.itemMeta = meta
        return this
    }

    fun type(material: Material): KItemStack {
        itemStack.type = material
        return this
    }

    fun amount(amount: Int): KItemStack {
        itemStack.amount = amount
        return this
    }

    fun flag(flag: org.bukkit.inventory.ItemFlag): KItemStack {
        val meta = itemStack.itemMeta
        meta?.addItemFlags(flag)
        itemStack.itemMeta = meta
        return this
    }

    fun hideUnbreakable(): KItemStack {
        return flag(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE)
    }

    fun unbreakable(value: Boolean): KItemStack {
        val meta = itemStack.itemMeta
        meta?.isUnbreakable = value
        itemStack.itemMeta = meta
        return this
    }

    fun flags(vararg flags: org.bukkit.inventory.ItemFlag): KItemStack {
        flags.forEach { flag(it) }
        return this
    }

    fun enchantment(enchantment: Enchantment, level: Int): KItemStack {
        itemStack.addUnsafeEnchantment(enchantment, level)
        return this
    }

    fun enchantments(vararg enchantments: Pair<Enchantment, Int>): KItemStack {
        enchantments.forEach { enchantment(it.first, it.second) }
        return this
    }

    fun hideAttributes(): KItemStack {
        return flag(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES)
    }

    fun build(): ItemStack {
        return itemStack
    }
}