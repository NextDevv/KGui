package com.nextdevv.kgui.api

import com.nextdevv.kgui.models.GuiBorder
import com.nextdevv.kgui.models.GuiButton
import com.nextdevv.kgui.models.Pages
import com.nextdevv.kgui.utils.Alignment
import com.nextdevv.kgui.utils.tac
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.CompletableFuture

class KGui(private val plugin: JavaPlugin) {
    private val builders: HashMap<Int, Builder> = hashMapOf()
    fun builder(id: Int): Builder {
        if(builders.containsKey(id)) {
            return builders[id]!!
        }

        val builder = Builder()
        Bukkit.getPluginManager().registerEvents(builder, plugin)
        builders[id] = builder
        return builder
    }

    @Suppress("MemberVisibilityCanBePrivate")
    class Builder : Listener {
        // Builder class to create Spigot inventory GUIs
        private var title: String = "Inventory"
        private var rows: Int = 4
        private var inventoryHolder: InventoryHolder? = null
        private var border: GuiBorder? = null
        private var maxPages: Int = 1
        var currentPage: Int = 1
        private var canInteract: Boolean = false
        private var pages: Pages = Pages()
        private var itemSetIndex = hashMapOf<Int, ItemStack>()
        private var buttons: HashMap<Int, GuiButton> = hashMapOf()
        private val cacheVariables = hashMapOf<String, Any>()
        private val itemStacks = mutableListOf<ItemStack>()
        private var itemStackClickListener: ((ItemStack, Player, Builder) -> Unit)? = null
        private val borderItemStacks = mutableListOf<ItemStack>()
        private var onCloseListener: ((Builder, Player) -> Unit)? = null
        private var onOpenListener: ((Builder, Player) -> Unit)? = null
        private var firstOpen = false
        private val responses: HashMap<UUID, String> = hashMapOf()
        private val waitingForPlayer: MutableList<UUID> = mutableListOf()
        private val conditionsButton: HashMap<Int, HashMap<String, Any>> = hashMapOf()
        private val conditionsItems: HashMap<Int, HashMap<String, Any>> = hashMapOf()

        /**
         * Fills all the slots with the itemStack given
         *
         * @param itemStack: ItemStack - The itemstack to fill the slots with
         * @return Builder
         */
        fun fill(itemStack: ItemStack): Builder {
            // Fills all the slots with the itemStack given
            // itemStack: ItemStack - The itemstack to fill the slots with
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
            // Sets if the player can interact with the inventory
            // canInteract: Boolean - If the player can interact with the inventory
            this.canInteract = canInteract
            return this
        }

        /**
         * Adds a listener to every itemstack added to the inventory
         *
         * @param onClick: (ItemStack, Player, Builder) -> Unit - The function to run when the itemstack is clicked
         * @return Builder
         */
        fun addItemStackClickListener(onClick: (ItemStack, Player, Builder) -> Unit): Builder {
            // Adds a listener to every itemstack added to the inventory
            // onClick: (ItemStack, Player, Builder) -> Unit - The function to run when the itemstack is clicked
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
            // Adds a button to the inventory
            // index: Int - The index to add the button at
            // guiButton: GuiButton - The button to add
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
            // Adds a button to the inventory
            // index: Int - The index to add the button at
            // itemStack: ItemStack - The itemstack to add
            // onClick: (ItemStack, Player, Builder) -> Unit - The function to run when the itemstack is clicked
            buttons[index] = GuiButton().setItemStack(itemStack).setOnClick { builder, player ->
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
            // Create a cache variable to access at any time
            // key: String - The key of the cache variable
            // value: Any - The value of the cache variable
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
        fun onClose(onCloseListener: (Builder, Player) -> Unit): Builder {
            // Listen to the inventory close event
            // onCloseListener: (Builder, Player) -> Unit - The function to run when the inventory is closed
            this.onCloseListener = onCloseListener
            return this
        }

        @EventHandler
        private fun onInventoryOpen(event: InventoryOpenEvent) {
            // Check if it's the same inventory
            if(event.view.title.tac() == title.tac()) {
                // call on open listener
                val player = event.player as Player
                onOpenListener?.let { it(this, player) }
            }
        }

        @EventHandler
        private fun onInventoryClose(event: InventoryCloseEvent) {
            // Check if it's the same inventory
            if(event.view.title.tac() == title.tac()) {
                // call on close listener
                onCloseListener?.let { it(this@Builder, event.player as Player) }
            }
        }

        @EventHandler
        private fun onInventoryClick(event: InventoryClickEvent) {
            val player = event.whoClicked as Player

            if(event.view.title.tac() == title.tac()) {
                if(event.currentItem != null) {
                    if(borderItemStacks.contains(event.currentItem)) {
                        event.isCancelled = true
                    }else {
                        event.isCancelled = !canInteract
                    }
                }else {
                    event.isCancelled = !canInteract
                }

                if(buttons.containsKey(event.rawSlot)) {
                    buttons[event.rawSlot]!!.onClick?.let { it(this, player) }
                }

                // Check if in the current slot there is an itemstack inside the itemStacks list then call the listener if not null
                if(itemStacks.contains(event.currentItem)) {
                    itemStackClickListener?.let { event.currentItem?.let { it1 -> it(it1, player, this) } }
                }
            }
        }

        @EventHandler
        private fun onPlayerChatEvent(event: AsyncPlayerChatEvent) {
            val player = event.player
            val uuid = player.uniqueId
            if(waitingForPlayer.contains(uuid)) {
                responses[uuid] = event.message
                waitingForPlayer.remove(uuid)
                event.isCancelled = true
            }
        }

        /**
         * Sets an item at inventory index
         *
         * @param itemStack: ItemStack - The item to set
         * @param index: Int - The index to set the item at
         * @return Builder
         */
        fun setItem(itemStack: ItemStack, index: Int): Builder {
            // Sets a item at inventory index
            // itemStack: ItemStack - The item to set
            // index: Int - The index to set the item at
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
         * Default: 4 rows
         * @param rows: Int - The number of rows in the inventory
         * @return Builder
         */
        fun setRows(rows: Int): Builder{
            this.rows = rows
            return this
        }
        
        /**
         * Sets the InventoryHolder of the Inventory GUI
         * 
         * Default: null
         * @param inventoryHolder: InventoryHolder - The InventoryHolder of the inventory
         * @return Builder
         */
        fun setInventoryHolder(inventoryHolder: InventoryHolder): Builder{
            // Set the InventoryHolder of the inventory
            // inventoryHolder: InventoryHolder - The InventoryHolder of the inventory
            this.inventoryHolder = inventoryHolder
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
            player.closeInventory()
            player.sendMessage(request.tac())
            waitingForPlayer.add(player.uniqueId)
            return CompletableFuture.supplyAsync {
                try {
                    val uuid = player.uniqueId
                    responses[uuid] = ""
                    while (responses[uuid]!!.isBlank()) {
                        Thread.sleep(1)
                    }
                    return@supplyAsync responses.remove(uuid)
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
         * @param border: GuiBorder - The border of the inventory
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
         * @param maxPages: Int - The max pages of the inventory
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
         * @param currentPage: Int - The current page of the inventory
         * @return Builder
         */
        fun setCurrentPage(currentPage: Int): Builder {
            // Set the current page of the inventory
            // currentPage: Int - The current page of the inventory
            this.currentPage = currentPage
            return this
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
         * MUST call this after closing the GUI
         */
        fun unregister() {
            HandlerList.unregisterAll(this)
        }

        /**
         * Builds the Inventory GUI
         * 
         * @return Inventory
         */
        fun build(): Inventory {
            val inventory = Bukkit.createInventory(inventoryHolder, rows * 9, ChatColor.translateAlternateColorCodes('&', title))
            if (border != null) {
                // Top border
                val border = border!!
                borderItemStacks.clear()
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

            // Set the buttons specified in the buttons hashmap
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
    }
}
