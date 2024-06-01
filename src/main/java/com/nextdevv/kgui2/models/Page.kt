package com.nextdevv.kgui2.models

import com.nextdevv.kgui2.KGui
import com.nextdevv.kgui2.annotations.Experimental
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Experimental
@OptIn(Experimental::class)
class Page {
    private var title: String = ""
    private var rows: Int = 0
    private var buttons: HashMap<Int, Button> = hashMapOf()
    private var border: Border? = Border()
    private var items: MutableMap<Int, ItemStack> = mutableMapOf()
    private var itemsCondition: MutableMap<Int, KGui.Builder.(Player) -> ItemStack?> = mutableMapOf()
    private var buttonsCondition: MutableMap<Int, KGui.Builder.(Player) -> Button?> = mutableMapOf()
    private var onClose: (Page.(Player) -> Unit)? = null

    fun onClose(action: Page.(Player) -> Unit): Page {
        onClose = action
        return this
    }
    
    fun button(slot: Int, button: Button): Page {
        buttons[slot] = button
        return this
    }
    
    fun item(slot: Int, item: ItemStack): Page {
        items[slot] = item
        return this
    }

    fun item(slot: Int, item: KGui.Builder.(Player) -> ItemStack?): Page {
        itemsCondition[slot] = item
        return this
    }

    fun button(slot: Int, button: KGui.Builder.(Player) -> Button?): Page {
        buttonsCondition[slot] = button
        return this
    }
    
    fun title(title: String): Page {
        this.title = title
        return this
    }
    
    fun rows(rows: Int): Page {
        this.rows = rows
        return this
    }
    
    fun border(border: Border?): Page {
        this.border = border
        return this
    }

    fun items(items: MutableMap<Int, ItemStack>): Page {
        this.items = items
        return this
    }

    fun itemsCondition(itemsCondition: MutableMap<Int, KGui.Builder.(Player) -> ItemStack?>): Page {
        this.itemsCondition = itemsCondition
        return this
    }

    fun buttonsCondition(buttonsCondition: MutableMap<Int, KGui.Builder.(Player) -> Button?>): Page {
        this.buttonsCondition = buttonsCondition
        return this
    }

    fun buttons(buttons: HashMap<Int, Button>): Page {
        this.buttons = buttons
        return this
    }
    
    fun build(): Page {
        return this
    }
}