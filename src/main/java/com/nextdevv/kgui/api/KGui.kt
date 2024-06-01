package com.nextdevv.kgui.api

import com.nextdevv.kgui.events.InventoryListener
import com.nextdevv.kgui.models.GuiBorder
import com.nextdevv.kgui.models.GuiButton
import com.nextdevv.kgui.models.Pages
import com.nextdevv.kgui.utils.Alignment
import com.nextdevv.kgui.utils.tac
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.CompletableFuture

private val inventories = hashMapOf<Inventory, Int>()

internal var Inventory.id: Int
    get() = inventories[this] ?: -1
    set(value) {
        inventories[this] = value
    }

@Suppress("unused")
class KGui(private val plugin: JavaPlugin) {
    internal val builders: MutableMap<UUID, MutableList<Builder>> = mutableMapOf()

    fun init() {
        Bukkit.getPluginManager().registerEvents(InventoryListener(this), plugin)
    }

    fun builder(player: Player): Builder {
        val builder = Builder(plugin, player, this)
        if (builders[player.uniqueId] == null) {
            builders[player.uniqueId] = mutableListOf(builder)
        } else {
            builders[player.uniqueId]!!.add(builder)
        }
        val indexOf = builders[player.uniqueId]!!.indexOf(builder)
        builder.builderNumber = indexOf
        return builder
    }

    @Suppress("MemberVisibilityCanBePrivate")
    class Builder(private val plugin: JavaPlugin, private val player: Player, private val kGui: KGui) : InventoryHolder {
        private var title: String = "Inventory"
        internal var builderNumber = 0
        internal var active = true
        private var rows: Int = 4
        private var border: GuiBorder? = null
        private var maxPages: Int = 1
        private var currentPage: Int = 1
        private var canInteract: Boolean = false
        private var pages: Pages = Pages()
        private var itemSetIndex = hashMapOf<Int, ItemStack>()
        private var buttons: HashMap<Int, GuiButton> = hashMapOf()
        private val cacheVariables = hashMapOf<String, Any>()
        private val itemStacks = mutableListOf<ItemStack>()
        private var itemStackClickListener: ((ItemStack, Player, Builder, ClickType) -> Unit)? = null
        private val borderItemStacks = mutableListOf<ItemStack>()
        private var onCloseListener: (Builder.(Builder, Player) -> Unit)? = null
        private var onOpenListener: (Builder.(Builder, Player) -> Unit)? = null
        private var firstOpen = false
        internal val responses: HashMap<UUID, String> = hashMapOf()
        internal val waitingForPlayer: MutableList<UUID> = mutableListOf()
        private val conditionsButton: HashMap<Int, HashMap<String, Any>> = hashMapOf()
        private val conditionsItems: HashMap<Int, HashMap<String, Any>> = hashMapOf()
        private var changeScreen = false
        private var permission: String? = null

        /**
         * Updates the inventory
         * @param func Builder.() -> Unit - The function to run
         * @param ticks Long - The number of ticks to wait before running the function
         */
        fun updater(func: Builder.() -> Unit, ticks: Long): Builder {
            Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
                func()
                update()
            }, 0, ticks)
            return this
        }

        /**
         * Updates the inventory
         * @param func Builder.() -> Unit - The function to run
         * @param ticks Long - The number of ticks to wait before running the function
         * @param delay Long - The number of ticks to wait before running the function
         */
        fun updater(func: Builder.() -> Unit, ticks: Long, delay: Long): Builder {
            Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
                func()
                update()
            }, delay, ticks)
            return this
        }

        /**
         * Updates the inventory
         */
        fun update(): Builder {
            val inventory = build()
            player.openInventory(inventory)
            return this
        }

        /**
         * If you change the current screen of the player,
         * the listener will be unregistered, and it will break the inventory
         * @param changeScreen: Boolean - If you change the current screen of the player,
         * the listener will be unregistered, and it will break the inventory
         * @return Builder
         */
        fun changeScreen(changeScreen: Boolean): Builder {
            this.changeScreen = changeScreen
            return this
        }

        /**
         * Fills all the slots with the itemStack given
         *
         * @param itemStack: ItemStack - The itemstack to fill the slots with
         * @return Builder
         */
        fun fill(itemStack: ItemStack): Builder {
            for (i in 0 until rows * 9) {
                itemSetIndex[i] = itemStack
            }
            return this
        }

        /**
         * Sets if the player can interact with the inventory
         *
         * Default: false
         * @param canInteract: Boolean - If the player can interact with the inventory
         * @return Builder
         */
        fun canInteract(canInteract: Boolean): Builder {
            this.canInteract = canInteract
            return this
        }

        /**
         * Adds a listener to every itemstack added to the inventory
         *
         * @param onClick: (ItemStack, Player, Builder) -> Unit - The function to run when the itemstack is clicked
         * @return Builder
         */
        fun addItemStackClickListener(onClick: (ItemStack, Player, Builder, ClickType) -> Unit): Builder {
            itemStackClickListener = onClick
            return this
        }

        /**
         * Adds a listener to every itemstack added to the inventory
         *
         * @param onClick: (ItemStack, Player, Builder) -> Unit - The function to run when the itemstack is clicked
         * @return Builder
         */
        fun addItemStackClick(onClick: (ItemStack, Player, Builder, ClickType) -> Unit): Builder {
            itemStackClickListener = onClick
            return this
        }

        /**
         * Adds a button to the Inventory GUI
         *
         * @param index: Int - The index to add the button at
         * @param guiButton: GuiButton - The button to add
         * @return Builder
         */
        fun addButton(index: Int, guiButton: GuiButton): Builder {
            buttons[index] = guiButton
            return this
        }

        /**
         * Adds a button to the Inventory GUI
         *
         * @param index: Int - The index to add the button at
         * @param itemStack: ItemStack - The itemstack to add
         * @param onClick: (ItemStack, Player, Builder) -> Unit - The function to run when the itemstack is clicked
         * @return Builder
         */
        fun addButton(index: Int, itemStack: ItemStack, onClick: (ItemStack, Player, Builder) -> Unit): Builder {
            buttons[index] = GuiButton().setItemStack(itemStack).onClick { builder, player ->
                onClick(itemStack, player, builder)
            }
            return this
        }

        /**
         * Create a cache variable to access at any time
         *
         * @param key: String - The key of the cache variable
         * @param value: Any - The value of the cache variable
         * @return Builder
         */
        fun createCacheVariable(key: String, value: Any): Builder {
            cacheVariables[key] = value
            return this
        }

        /**
         * Get a cache variable
         *
         * @param key: String - The key of the cache variable
         * @return Any
         */
        fun <T> getCacheVariable(key: String): T? {
            // Get a cache variable
            // key: String - The key of the cache variable
            return cacheVariables[key] as T?
        }

        /**
         * Remove a cache variable
         *
         * @param key: String - The key of the cache variable
         * @return Builder
         */
        fun removeCacheVariable(key: String): Builder {
            // Remove a cache variable
            // key: String - The key of the cache variable
            cacheVariables.remove(key)
            return this
        }

        /**
         * Set a cache variable
         *
         * @param key: String - The key of the cache variable
         * @param value: Any - The value of the cache variable
         * @return Builder
         */
        fun setCacheVariable(key: String, value: Any): Builder {
            // Set a cache variable
            // key: String - The key of the cache variable
            // value: Any - The value of the cache variable
            cacheVariables[key] = value
            return this
        }

        /**
         * Adds a button to the Inventory GUI
         *
         * @param alignment: Alignment - The Alignment to add the button at
         * @param guiButton: GuiButton - The button to add
         * @return Builder
         */
        fun addButton(alignment: Alignment, guiButton: GuiButton): Builder {
            buttons[Alignment.getIndex(alignment, rows)] = guiButton
            return this
        }

        /**
         * Displays the button if the condition provided is met
         *
         * @param index: Int - The index to add the button at
         * @param guiButton: GuiButton - The button to add
         * @param condition: (Player) -> Boolean - The condition to check
         */
        fun addButtonWithCondition(index: Int, guiButton: GuiButton, condition: Builder.() -> Boolean): Builder {
            conditionsButton[index] = hashMapOf("button" to guiButton, "condition" to condition)
            return this
        }

        /**
         * Displays the button if the condition provided is met
         *
         * @param alignment:  Alignment - The Alignment to add the button at
         * @param guiButton: GuiButton - The button to add
         * @param condition: (Player) -> Boolean - The condition to check
         */
        fun addButtonWithCondition(alignment: Alignment, guiButton: GuiButton, condition: Builder.() -> Boolean): Builder {
            conditionsButton[Alignment.getIndex(alignment, rows)] = hashMapOf("button" to guiButton, "condition" to condition)
            return this
        }

        /**
         * Listen to the inventory close event
         *
         * @param onCloseListener: (Builder, Player) -> Unit - The function to run when the inventory is closed
         * @return Builder
         */
        fun onClose(onCloseListener: Builder.(Builder, Player) -> Unit): Builder {
            // Listen to the inventory close event
            // onCloseListener: (Builder, Player) -> Unit - The function to run when the inventory is closed
            this.onCloseListener = onCloseListener
            return this
        }

        /**
         * Listen to the inventory open event
         *
         * @param onOpenListener: Builder.(Builder, Player) -> Unit - The function to run when the inventory is opened
         * @return Builder
         */
        fun onOpen(onOpenListener: Builder.(Builder, Player) -> Unit): Builder {
            // Listen to the inventory open event
            // onOpenListener: Builder.(Builder, Player) -> Unit - The function to run when the inventory is opened
            this.onOpenListener = onOpenListener
            return this
        }

        internal fun onInventoryOpen(player: Player) {
            // Check if it's the same inventory
            onOpenListener?.let { it(this, player) }
        }

        /**
         * Empty the inventory
         */
        fun empty(): Builder {
            itemStacks.clear()
            itemSetIndex.clear()
            buttons.clear()
            conditionsItems.clear()
            conditionsButton.clear()
            return this
        }


        /**
         * Sets the itemstacks of the inventory from index X to index Y
         *
         * @param itemStacks: List<ItemStack> - The itemstacks to set
         * @param from: Int - The index to start setting the itemstacks at
         * @param to: Int - The index to stop setting the itemstacks at
         */
        fun setItemStacks(itemStacks: List<ItemStack>, from: Int, to: Int): Builder {
            for (i in from .. to) {
                this.itemStacks.add(itemStacks[i])
            }
            return this
        }

        internal fun onInventoryClose(player: Player) {
            onCloseListener?.let { it(this@Builder, player) }
        }

        internal fun onInventoryClick(player: Player, clickType: ClickType, slot: Int, currentItemStack: ItemStack?): Boolean {
            var returnBool = false
            currentItemStack?.let {
                returnBool = if(it in borderItemStacks) {
                    true
                }else !canInteract
            } ?: run {
                returnBool = !canInteract
            }

            if(slot in buttons.keys) {
                buttons[slot]?.let { button ->
                    when (clickType) {
                        ClickType.RIGHT -> {
                            button.getOnRightClick()?.let { it(this, player) }
                        }
                        ClickType.LEFT -> {
                            button.getOnClick()?.let { it(this, player) }
                        }
                        else -> { }
                    }
                }
            }

            if(currentItemStack in itemStacks) {
                itemStackClickListener?.let { onClick ->
                    currentItemStack?.let { onClick(it, player, this, clickType) }
                }
            }

            return returnBool
        }

        /**
         * Sets an item at inventory index
         *
         * @param itemStack: ItemStack - The item to set
         * @param index: Int - The index to set the item at
         * @return Builder
         */
        fun setItem(itemStack: ItemStack, index: Int): Builder {
            itemSetIndex[index] = itemStack
            return this
        }

        /**
         * Sets an item at inventory index
         *
         * @param itemStack: ItemStack - The item to set
         * @param alignment: Alignment - The alignment to set the item at
         * @return Builder
         */
        fun setItem(itemStack: ItemStack, alignment: Alignment): Builder {
            itemSetIndex[Alignment.getIndex(alignment, rows)] = itemStack
            return this
        }

        /**
         * Sets an item at inventory index if the condition provided is met
         *
         * @param itemStack: ItemStack - The item to set
         * @param index: Int - The index to set the item at
         * @param condition: Builder.() -> Boolean - The condition to check
         */
        fun setItem(itemStack: ItemStack, index: Int, condition: Builder.() -> Boolean): Builder {
            conditionsItems[index] = hashMapOf("item" to itemStack, "condition" to condition)
            return this
        }

        /**
         * Sets an item at inventory index if the condition provided is met
         *
         * @param itemStack: ItemStack - The item to set
         * @param alignment: Alignment - The alignment to set the item at
         * @param condition: Builder.() -> Boolean - The condition to check
         */
        fun setItem(itemStack: ItemStack, alignment: Alignment, condition: Builder.() -> Boolean): Builder {
            conditionsItems[Alignment.getIndex(alignment, rows)] =
                hashMapOf("item" to itemStack, "condition" to condition)
            return this
        }

        /**
         * Sets the row of the Inventory GUI that is 9 slots wide
         * 
         * Default: Four rows
         * @param rows: Int - The number of rows in the inventory
         * @return Builder
         */
        fun setRows(rows: Int): Builder{
            this.rows = rows
            return this
        }
        
        /**
         * Sets the title of the Inventory GUI
         * 
         * Default: "Inventory"
         * @param title: String - The title of the inventory
         * @return Builder
         */
        fun setTitle(title: String): Builder{
            // Set the title of the inventory
            // title: String - The title of the inventory
            this.title = title
            return this
        }

        /**
         * Asks for input from the player
         *
         * @param player: Player - The player to ask for input from
         * @param request: String - The request to ask the player
         * @return String
         */
        fun askForInput(player: Player, request: String): CompletableFuture<String> {
            waitingForPlayer.add(player.uniqueId)

            player.closeInventory()
            player.sendMessage(request.tac())
            return CompletableFuture.supplyAsync {
                try {
                    val uuid = player.uniqueId
                    while (responses[uuid].isNullOrBlank()) {
                        Thread.sleep(1)
                    }
                    return@supplyAsync responses[uuid]
                }catch (e: Exception) {
                    e.printStackTrace()
                    return@supplyAsync ""
                }
            }
        }
        
        /**
         * Sets the border of the Inventory GUI
         * 
         * Default: null
         * @param border GuiBorder - The border of the inventory
         * @return Builder
         */
        fun setBorder(border: GuiBorder): Builder {
            // Set the border of the inventory
            // border: GuiBorder - The border of the inventory
            this.border = border
            return this
        }

        /**
         * Sets the max pages of the Inventory GUI
         *
         * Default: 1
         * @param maxPages Int - The max pages of the inventory
         * @return Builder
         */
        fun setMaxPages(maxPages: Int): Builder {
            // Set the max pages of the inventory
            // maxPages: Int - The max pages of the inventory
            this.maxPages = maxPages
            return this
        }

        /**
         * Sets the current page of the Inventory GUI
         *
         * Default: 1
         * @param currentPage Int - The current page of the inventory
         * @return Builder
         */
        fun setCurrentPage(currentPage: Int): Builder {
            // Set the current page of the inventory
            // currentPage: Int - The current page of the inventory
            this.currentPage = currentPage
            return this
        }

        /**
         * Gets the current page of the Inventory GUI
         */
        fun getCurrentPage(): Int {
            return currentPage
        }

        /**
         * Sets the permission to open the Inventory GUI
         * @param permission String - The permission to open the inventory
         * @return [Builder]
         */
        fun setPermission(permission: String?): Builder {
            this.permission = permission
            return this
        }

        /**
         * Gets the permission to open the Inventory GUI
         */
        fun getPermission(): String? {
            return permission
        }

        /**
         * Sets the pages of the Inventory GUI
         *
         * Default: Pages()
         * @param pages: Pages - The pages of the inventory
         * @return Builder
         */
        fun setPages(pages: Pages): Builder {
            // Set the pages of the inventory
            // pages: Pages - The pages of the inventory
            this.pages = pages
            return this
        }

        fun calculateFreeSlots(): Int {
            // Calculates the free slots in the inventory
            var freeSlots = 0
            for (i in 0 until rows * 9) {
                if (itemSetIndex[i] == null || itemSetIndex[i]!!.type.isAir) {
                    freeSlots++
                }
            }
            return freeSlots
        }

        private fun applyToBorderStack(item: ItemStack) {
            item.apply {
                val itemMeta = itemMeta
                itemMeta?.setDisplayName(" ")
                itemMeta?.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                itemMeta?.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)

                this.setItemMeta(itemMeta)
            }
        }

        /**
         * Builds the Inventory GUI
         * 
         * @return Inventory
         */
        fun build(): Inventory {
            val inventory = Bukkit.createInventory(player, rows * 9, ChatColor.translateAlternateColorCodes('&', title))
            inventory.id = builderNumber
            itemStacks.clear()
            borderItemStacks.clear()

            if (border != null) {
                // Top border
                val border = border!!
                borderItemStacks.add(border.defaultItemStack)
                applyToBorderStack(border.defaultItemStack)

                if(border.topOffset >= 0) {
                    if (border.topItemStack != null) {
                        applyToBorderStack(border.topItemStack!!)
                        borderItemStacks.add(border.topItemStack!!)
                        for (i in (9 * (border.topOffset+1))-9 until 9 * (border.topOffset+1)) {
                            inventory.setItem(i, border.topItemStack)
                        }
                    } else {
                        for (i in (9 * (border.topOffset+1))-9 until 9 * (border.topOffset+1)) {
                            inventory.setItem(i, border.defaultItemStack)
                        }
                    }
                }
                // Bottom border
                if(border.bottomOffset >= 0) {
                    if (border.bottomItemStack != null) {
                        applyToBorderStack(border.bottomItemStack!!)
                        borderItemStacks.add(border.bottomItemStack!!)
                        for (i in (inventory.size - 1) - (9* border.bottomOffset) downTo ((inventory.size - 1) - (9* border.bottomOffset))-8) {
                            inventory.setItem(i, border.bottomItemStack)
                        }
                    } else {
                        for (i in (inventory.size - 1) - (9* border.bottomOffset) downTo ((inventory.size - 1) - (9* border.bottomOffset))-8) {
                            inventory.setItem(i, border.defaultItemStack)
                        }
                    }
                }
                // Left border
                if(border.topOffset >= 0) {
                    if (border.leftItemStack != null) {
                        applyToBorderStack(border.leftItemStack!!)
                        borderItemStacks.add(border.leftItemStack!!)
                        for (i in 0 until rows) {
                            inventory.setItem((i * 9) + border.leftOffset, border.leftItemStack)
                        }
                    } else {
                        for (i in 0 until rows) {
                            inventory.setItem((i * 9) + border.leftOffset, border.defaultItemStack)
                        }
                    }
                }
                // Right border
                if(border.rightOffset >= 0) {
                    if (border.rightItemStack != null) {
                        applyToBorderStack(border.rightItemStack!!)
                        borderItemStacks.add(border.rightItemStack!!)
                        for (i in 0 until rows) {
                            inventory.setItem((i * 9 + 8) - border.rightOffset, border.rightItemStack)
                        }
                    } else {
                        for (i in 0 until rows) {
                            inventory.setItem((i * 9 + 8) - border.rightOffset, border.defaultItemStack)
                        }
                    }
                }
            }

            // Set the buttons specified in the conditionsButton hashmap
            for (index in conditionsButton.keys) {
                val condition = conditionsButton[index]!!["condition"] as Builder.() -> Boolean
                if(condition()) {
                    buttons[index] = conditionsButton[index]!!["button"] as GuiButton
                }else {
                    buttons.remove(index)
                }
            }

            // Set the items specified in the conditionsItems hashmap
            for (index in conditionsItems.keys) {
                val condition = conditionsItems[index]!!["condition"] as Builder.() -> Boolean
                if(condition()) {
                    itemSetIndex[index] = conditionsItems[index]!!["item"] as ItemStack
                }else {
                    itemSetIndex.remove(index)
                }
            }

            // Set the items specified in the itemSetIndex hashmap
            for (index in itemSetIndex.keys) {
                inventory.setItem(index, itemSetIndex[index])
                itemStacks.add(itemSetIndex[index]!!)
            }

            // Set the buttons specified in the button hashmap
            for (index in buttons.keys) {
                inventory.setItem(index, buttons[index]!!.getItemStack())
            }

            // Set the items of the gui
            if (pages.pages.size >= currentPage) {
                for (itemStack in pages.pages[currentPage - 1].getContent()) {
                    inventory.addItem(itemStack)
                    itemStacks.add(itemStack)
                }
            }

            return inventory
        }

        override fun getInventory(): Inventory {
            return build()
        }
    }
}
