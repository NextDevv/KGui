package com.nextdevv.kgui2.models

import com.nextdevv.kgui2.annotations.Experimental
import org.bukkit.inventory.ItemStack

@Experimental
@OptIn(Experimental::class)
class Pages {
    private var pages: MutableMap<Int, Page> = mutableMapOf()
    private var current = 0
    private var max = 7
    private var main = Page()

    /**
     * Create a new page with the given index and page.
     * @param index The index of the page.
     * @param page The page to add.
     */
    fun page(index: Int, page: Page): Pages {
        pages[index] = page
        return this
    }

    fun mainPage(page: Page): Pages {
        main = page
        return this
    }

    /**
     * Create a new page with the given index and apply the given lambda to it.
     * @param index The index of the page.
     * @param page The lambda to apply to the page.
     * @return The Pages object.
     */
    fun page(index: Int, page: Page.() -> Unit): Pages {
        pages[index] = Page().apply(page)
        return this
    }

    fun maxPerPage(max: Int): Pages {
        this.max = max
        return this
    }

    fun autoCreatePages(list: MutableList<ItemStack>) {
        var index = 0
        var page = Page()
        for (item in list) {
            if (index == max) {
                pages[current] = page
                page = Page()
                index = 0
                current++
            }
            page.item(index, item)
            index++
        }
        pages[current] = page
    }

    fun getPage(index: Int): Page? {
        return pages[index]
    }

    fun getPages(): MutableMap<Int, Page> {
        return pages
    }
}