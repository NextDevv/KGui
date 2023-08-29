package com.nextdevv.kgui.utils

import org.bukkit.ChatColor

fun String.tac(char: Char = '&'): String {
    return ChatColor.translateAlternateColorCodes(char, this)
}