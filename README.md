# KGui - Easy Spigot Inventory GUI Creation
KGui is a powerful and user-friendly API for creating inventory GUIs in Spigot plugins. It simplifies the process of designing interactive and dynamic GUIs for your Minecraft server, allowing you to focus on creating engaging player experiences without getting bogged down in complex GUI code.

# Docs
https://docs.nextdevv.com/

## Features
- Simple and intuitive API for GUI creation.
- Easily add buttons, items, and interactive elements to your GUIs.
- Flexible customization options for GUI appearance.
- Pagination support for navigating through lists of items.
- Built-in event listeners for item interactions.

## Getting Started
To get started with KGui, follow these steps:
1. **Dependency Setup:** Add KGui to your project's dependencies. You can either compile the library yourself or use a dependency management tool like Maven or Gradle.
3. **Import KGui Classes:** Import the necessary classes at the beginning of your plugin class or wherever you intend to use KGui:
```JAVA
import com.nextdevv.kgui.api.KGui;
import com.nextdevv.kgui.models.GuiButton;
import com.nextdevv.kgui.item.KItemStack;
import com.nextdevv.kgui.models.GuiBorder;
import com.nextdevv.kgui.models.Pages;
```
3. **Creating a Basic GUI:**

  **java**
```JAVA
// Create a GUI border
GuiBorder border = new GuiBorder();
border.setDefaultItemStack(new ItemStack(Material.RED_STAINED_GLASS_PANE));

// Build the GUI
KGui kGui = new KGui(PLUGIN);
kGui.init()
Inventory gui = kGui.builder(PLAYER)
    .setTitle("&cItem Search")
    .setRows(6)
    .setBorder(border)
    .addButton(Alignment.BOTTOM_CENTER, new GuiButton().setItemStack(
        KItemStack.builder()
            .setName("&c&lSearch")
            .setMaterial(Material.COMPASS)
            .build()
    ).setOnClick((builder, player) -> {
        // Handle search button click
        // ...
    }))
    .addItemStackClickListener((itemStack, player, builder) -> {
        // Handle item click
        // ...
    })
    .build();
```
  **kotlin**
```KOTLIN
// Create a border
val border = GuiBorder()
border.defaultItemStack = ItemStack(Material.RED_STAINED_GLASS_PANE)


// Build the GUI
val kGui = KGui(PLUGIN)
kGui.init()
val gui: Inventory = kGui.builder(PLAYER)
  .setTitle("&cItem Search")
  .setRows(6)
  .setBorder(border)
  .addButton(Alignment.BOTTOM_CENTER, GuiButton().setItemStack(
      KItemStack.builder()
        .setName("&c&lSearch")
        .setMaterial(Material.COMPASS)
        .build()
  ).setOnClick { builder: KGui.Builder, player: Player -> 
  // Handle the click event
  })
  .addItemStackClickListener { itemStack: ItemStack, player: Player, builder: KGui.Builder -> 
    // Handle the click event on the item
  }
  .build()
```
1. **Customize and Add Buttons:** You can add more buttons and customize your GUI's appearance and behavior using the provided methods like **`addButton`**, **`setTitle`**, and **`setCurrentPage`**.
2. **Pagination Support:** The example code already demonstrates pagination using the "Previous Page" and "Next Page" buttons. You can extend this to display paginated content dynamically.
3. **Event Handling:** Implement the click event handlers for your buttons and items using lambda expressions or dedicated methods.
4. **Show GUI to Players:**
```JAVA
player.openInventory(gui);
```

## Contribution
Contributions to KGui are welcome! If you find any issues or have suggestions for improvements, feel free to submit a pull request or create an issue on the GitHub repository.

## License
KGui is licensed under the __**MIT License**__. Feel free to use, modify, and distribute this library in your projects.

## Contact
For questions, support, or discussions, you can reach out to the project maintainers through the GitHub repository's issue section.

<br>

With KGui, creating interactive inventory GUIs for your Spigot server has never been easier. Enjoy a streamlined development process and enhance your players' experience with engaging and user-friendly interfaces.
