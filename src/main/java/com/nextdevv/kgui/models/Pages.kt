package com.nextdevv.kgui.models

import org.bukkit.inventory.ItemStack

class Pages {
    val pages: MutableList<GuiPage> = mutableListOf()

    /**
     * Adds a page to the Pages object
     *
     * @param guiPage: GuiPage - The page to add
     */
    fun addPage(guiPage: GuiPage): Pages {
        pages.add(guiPage)
        return this
    }

    /**
     * Adds a page to the Pages object at a specific index
     *
     * @param guiPage: GuiPage - The page to add
     * @param index: Int - The index to add the page at
     */
    fun addPage(guiPage: GuiPage, index: Int): Pages {
        pages.add(index, guiPage)
        return this
    }

    /**
     * Removes a page from the Pages object at a specific index
     *
     * @param index: Int - The index to remove the page at
     */
    fun removePage(index: Int): Pages {
        pages.removeAt(index)
        return this
    }

    /**
     * Removes a page from the Pages object
     *
     * @param guiPage: GuiPage - The page to remove
     */
    fun removePage(guiPage: GuiPage): Pages {
        pages.remove(guiPage)
        return this
    }

    /**
     * Filters the pages in the Pages object
     *
     * @param predicate: (GuiPage) -> Boolean - The predicate to filter the pages
     */
    fun filterPages(predicate: (GuiPage) -> Boolean): Pages {
        pages.filter(predicate)
        return this
    }

    /**
     * Builder, used to create a Pages object
     *
     * @return Builder
     */
    fun builder(): Builder {
        return Builder()
    }

    class Builder() {
        private val pages: MutableList<GuiPage> = mutableListOf()
        private var maxPerPages = 10

        /**
         * Automatically creates GuiPages given the list of ItemStack
         */
        fun autoCreatePages(itemStacks: List<ItemStack>): Builder {
            var guiPage = GuiPage()
            for (itemStack in itemStacks) {
                if (guiPage.getContent().size >= maxPerPages) {
                    pages.add(guiPage)
                    guiPage = GuiPage()
                }
                guiPage.addItem(itemStack)
            }
            pages.add(guiPage)
            return this
        }

        /**
         * Sets the max per pages of the Pages object
         *
         * Default: 10
         * @param maxPerPages: Int - The max per pages of the Pages object
         * @return Builder
         */
        fun setMaxPerPages(maxPerPages: Int): Builder {
            // Set the max per pages of the Pages object
            // maxPerPages: Int - The max per pages of the Pages object
            this.maxPerPages = maxPerPages
            return this
        }

        fun build(): Pages {
            val pages = Pages()
            pages.pages.addAll(this.pages)
            return pages
        }
    }
}
