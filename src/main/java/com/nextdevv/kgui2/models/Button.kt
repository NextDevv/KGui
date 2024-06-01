package com.nextdevv.kgui2.models

import com.nextdevv.kgui2.KGui
import com.nextdevv.kgui2.annotations.Experimental
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@OptIn(Experimental::class)
class Button {
    var itemStack: ItemStack = ItemStack(Material.AIR)
    private var onClick: (Button.(Player, KGui.Builder) -> Unit)? = null
    private var onRightClick: (Button.(Player, KGui.Builder) -> Unit)? = null

    fun onClick(action: Button.(Player, KGui.Builder) -> Unit): Button {
        onClick = action
        return this
    }

    fun onRightClick(action: Button.(Player, KGui.Builder) -> Unit): Button {
        onRightClick = action
        return this
    }

    fun click(player: Player, builder: KGui.Builder) {
        onClick?.invoke(this, player, builder)
    }

    fun rightClick(player: Player, builder: KGui.Builder) {
        onRightClick?.invoke(this, player, builder)
    }
}