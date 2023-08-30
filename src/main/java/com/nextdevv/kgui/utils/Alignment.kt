package com.nextdevv.kgui.utils

enum class Alignment {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    companion object {
        fun getIndex(alignment: Alignment, rows: Int): Int {
            when(alignment) {
                TOP_LEFT -> return 0
                TOP_CENTER -> return 4
                TOP_RIGHT -> return 8
                CENTER_LEFT -> {
                    val size = rows * 9
                    val centerIndex = (size / 2) - 5
                    return centerIndex - 4
                }
                CENTER -> {
                    val size = rows * 9
                    return (size / 2) - 5
                }
                CENTER_RIGHT -> {
                    val size = rows * 9
                    val centerIndex = (size / 2) - 5
                    return centerIndex + 4
                }
                BOTTOM_LEFT -> {
                    val size = rows * 9
                    return size - 9
                }
                BOTTOM_CENTER -> {
                    val size = rows * 9
                    return (size - 9) + 4
                }
                BOTTOM_RIGHT -> {
                    val size = rows * 9
                    return (size - 9) + 8
                }
            }
        }
    }
}