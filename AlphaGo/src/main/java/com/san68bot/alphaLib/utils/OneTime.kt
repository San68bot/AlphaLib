package com.san68bot.alphaLib.utils

class OneTime {
    private var oneTimeVar = true

    infix fun once(block: () -> Unit) {
        if (oneTimeVar) {
            oneTimeVar = false
            block()
        }
    }

    fun reset() { oneTimeVar = true }

    fun isActive() = oneTimeVar
}