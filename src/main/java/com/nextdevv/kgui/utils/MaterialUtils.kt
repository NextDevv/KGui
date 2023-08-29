package com.nextdevv.kgui.utils

import org.bukkit.Material

fun randomMaterial(): Material {
    return Material.entries.toTypedArray().random()
}