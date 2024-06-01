package com.nextdevv.kgui2

import com.nextdevv.kgui.utils.tac
import com.nextdevv.kgui2.annotations.Experimental
import com.nextdevv.kgui2.events.ItemClickEvent
import com.nextdevv.kgui2.models.Border
import com.nextdevv.kgui2.models.Button
import com.nextdevv.kgui2.models.Page
import com.nextdevv.kgui2.models.Pages
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

@Experimental
class KGui(private val plugin: JavaPlugin): Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private val builders = mutableListOf<Builder>()

    private fun createBuilder(uuid: UUID): Builder {
        val builder = Builder(uuid)
        builders.add(builder)
        return builder
    }

    fun createBuilder(player: Player): Builder {
        return createBuilder(player.uniqueId)
    }

    @EventHandler
    fun onInventoryClickEvent(e: InventoryClickEvent) {
        val inv = e.clickedInventory ?: return
        if (inv.holder !is Player) return

        try {
            builders.firstOrNull { it.uuid == (inv.holder as Player).uniqueId }?.let {
                val builder = it
                e.isCancelled = builder.onInventoryClick(inv.holder as Player, e.click, e.rawSlot, e.currentItem)
            }
        } catch (_: Exception) {}
    }

    @EventHandler
    fun onInventoryCloseEvent(e: InventoryClickEvent) {
        val inv = e.clickedInventory ?: return
        if (inv.holder !is Player) return

        try {
            builders.firstOrNull { it.uuid == (inv.holder as Player).uniqueId }?.let {
                val builder = it
                builder.onInventoryClose(inv.holder as Player)
            }
        } catch (_: Exception) {}
    }

    @EventHandler
    fun onInventoryOpenEvent(e: InventoryOpenEvent) {
        val inv = e.inventory
        if (inv.holder !is Player) return

        try {
            builders.firstOrNull { it.uuid == (inv.holder as Player).uniqueId }?.let {
                val builder = it
                builder.onInventoryOpen(inv.holder as Player)
            }
        } catch (_: Exception) {}
    }

    class Builder(val uuid: UUID) {
        private var title = ""
        private var rows = 3
        private var items = hashMapOf<Int, ItemStack>()
        private var itemsCondition = hashMapOf<Int, Builder.(Player) -> ItemStack?>()
        private var border: Border? = null
        private var buttons = hashMapOf<Int, Button>()
        private var buttonsCondition = hashMapOf<Int, Builder.(Player) -> Button?>()
        private var mainPage = Page()
        private var pages = Pages()
        private var canInteract = false
        private var onInventoryCloseEvent: (Builder.(Player) -> Unit)? = null
        private var onInventoryOpenEvent: (Builder.(Player) -> Unit)? = null
        private var itemListener: (ItemClickEvent) -> Unit = {}

        fun interact(value: Boolean): Builder {
            canInteract = value
            return this
        }

        fun onInteract(action: (ItemClickEvent) -> Unit): Builder {
            itemListener = action
            return this
        }

        fun onInventoryClose(action: Builder.(Player) -> Unit): Builder {
            onInventoryCloseEvent = action
            return this
        }

        fun onInventoryOpen(action: Builder.(Player) -> Unit): Builder {
            onInventoryOpenEvent = action
            return this
        }

        fun title(title: String): Builder {
            this.title = title
            return this
        }

        fun rows(rows: Int): Builder {
            this.rows = rows
            return this
        }

        fun item(slot: Int, item: ItemStack): Builder {
            items[slot] = item
            return this
        }

        fun item(slot: Int, item: Builder.(Player) -> ItemStack?): Builder {
            itemsCondition[slot] = item
            return this
        }

        fun border(border: Border): Builder {
            this.border = border
            return this
        }

        fun button(slot: Int, button: Button): Builder {
            buttons[slot] = button
            return this
        }

        fun button(slot: Int, button: Builder.(Player) -> Button?): Builder {
            buttonsCondition[slot] = button
            return this
        }

        fun mainPage(page: Page): Builder {
            mainPage = page
            return this
        }

        fun display(page: Page) {
            Bukkit.getPlayer(uuid)?.openInventory(build(page))
        }

        fun build(page: Page = mainPage): Inventory {
            val inv = Bukkit.createInventory(null, rows * 9, title.tac())
            page
                .rows(rows)
                .title(title)
                .border(border)
                .items(items)
                .buttons(buttons)
                .itemsCondition(itemsCondition)
                .buttonsCondition(buttonsCondition)
                .build()

            pages.mainPage(mainPage)

            items.forEach { (slot, item) -> inv.setItem(slot, item) }
            border?.let {
                for (i in 0 until rows * 9) {
                    if (i < 9 || i >= (rows - 1) * 9 || i % 9 == 0 || i % 9 == 8) {
                        inv.setItem(i, it.defaultItemStack)
                    }
                }

                // INV SIZE: 54 (9x6)
                // 0  1  2  3  4  5  6  7  8
                // 9  10 11 12 13 14 15 16 17
                // 18 19 20 21 22 23 24 25 26
                // 27 28 29 30 31 32 33 34 35
                // 36 37 38 39 40 41 42 43 44
                // 45 46 47 48 49 50 51 52 53

                for (i in 0 until rows) {
                    inv.setItem((i * 9) + it.leftOffset, it.leftItemStack ?: it.defaultItemStack)
                }

                for (i in 0 until rows) {
                    inv.setItem(((i * 9) + 8) - it.rightOffset, it.rightItemStack ?: it.defaultItemStack)
                }

                for (i in 0 until 9) {
                    if (it.topOffset > 0) {
                        inv.setItem(i + (it.topOffset * 9), it.topItemStack ?: it.defaultItemStack)
                    } else {
                        inv.setItem(i, it.topItemStack ?: it.defaultItemStack)
                    }
                }

                for (i in 0 until 9) {
                    if (it.bottomOffset > 0) {
                        inv.setItem(
                            i + ((rows - it.bottomOffset) * 9), it.bottomItemStack ?: it.defaultItemStack
                        )
                    } else {
                        inv.setItem(i + ((rows - 1) * 9), it.bottomItemStack ?: it.defaultItemStack)
                    }
                }
            }

            buttons.forEach { (slot, button) -> inv.setItem(slot, button.itemStack) }

            itemsCondition.forEach { (slot, item) -> inv.setItem(slot, Bukkit.getPlayer(uuid)
                ?.let { item.invoke(this, it) }) }
            buttonsCondition.forEach { (slot, button) -> inv.setItem(slot, Bukkit.getPlayer(uuid)
                ?.let { buttons[slot] = button.invoke(this, it)!!; buttons[slot]?.itemStack }) }

            return inv
        }

        fun open() {
            Bukkit.getPlayer(uuid)?.openInventory(build())
        }

        internal fun onInventoryClick(player: Player, click: ClickType, rawSlot: Int, currentItem: ItemStack?): Boolean {
            val button = buttons[rawSlot] ?: return false
            when(click) {
                ClickType.LEFT -> button.click(player, this)
                ClickType.RIGHT -> button.rightClick(player, this)

                else -> return canInteract
            }
            itemListener.invoke(
                ItemClickEvent(player, currentItem ?: ItemStack(Material.AIR), rawSlot, click, this)
            )
            return canInteract
        }

        fun onInventoryClose(player: Player) {
            onInventoryCloseEvent?.invoke(this, player)
        }

        fun onInventoryOpen(player: Player) {
            onInventoryOpenEvent?.invoke(this, player)
        }
    }
}