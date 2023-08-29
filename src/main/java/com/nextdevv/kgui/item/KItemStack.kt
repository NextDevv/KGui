package com.nextdevv.kgui.item

import com.nextdevv.kgui.utils.randomMaterial
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class KItemStack {
    fun builder(): Builder {
        return Builder()
    }

    class Builder {
        private var name: String = ""
        private var lore: MutableList<String> = mutableListOf()
        private var material: Material = Material.BARRIER
        private var amount: Int = 1
        private val enchants: MutableList<Enchantment> = mutableListOf()
        private val itemFlags: MutableList<ItemFlag> = mutableListOf()

        fun getListOfRandom(amount: Int): List<ItemStack> {
            val list = mutableListOf<ItemStack>()
            for(i in 0 until amount) {
                list.add(setMaterial(randomMaterial()).build())
            }
            return list
        }

        fun addItemFlag(itemFlag: ItemFlag): Builder {
            itemFlags.add(itemFlag)
            return this
        }

        fun addItemFlags(itemFlags: MutableList<ItemFlag>): Builder {
            this.itemFlags.addAll(itemFlags)
            return this
        }

        fun removeItemFlag(itemFlag: ItemFlag): Builder {
            itemFlags.remove(itemFlag)
            return this
        }

        fun removeItemFlags(itemFlags: MutableList<ItemFlag>): Builder {
            this.itemFlags.removeAll(itemFlags)
            return this
        }

        fun setName(name: String): Builder {
            this.name = name
            return this
        }

        fun setLore(lore: MutableList<String>): Builder {
            this.lore = lore
            return this
        }

        fun setLore(vararg lore: String): Builder {
            this.lore = lore.toMutableList()
            return this
        }

        fun setMaterial(material: Material): Builder {
            this.material = material
            return this
        }

        fun setAmount(amount: Int): Builder {
            this.amount = amount
            return this
        }

        fun addEnchantment(enchantment: Enchantment): Builder {
            enchants.add(enchantment)
            return this
        }

        fun addEnchantments(enchantments: MutableList<Enchantment>): Builder {
            enchants.addAll(enchantments)
            return this
        }

        fun removeEnchantment(enchantment: Enchantment): Builder {
            enchants.remove(enchantment)
            return this
        }

        fun removeEnchantments(enchantments: MutableList<Enchantment>): Builder {
            enchants.removeAll(enchantments)
            return this
        }

        fun build(): ItemStack {
            val itemStack = ItemStack(material)
            val itemMeta = itemStack.itemMeta

            itemMeta?.setDisplayName(ChatColor.translateAlternateColorCodes('&', name))
            itemMeta?.lore = lore.map { ChatColor.translateAlternateColorCodes('&', it) }
            enchants.forEach { enchantment -> itemMeta?.addEnchant(enchantment, 1, true) }
            itemFlags.forEach { itemFlag -> itemMeta?.addItemFlags(itemFlag) }

            itemStack.itemMeta = itemMeta

            return itemStack
        }
    }
}