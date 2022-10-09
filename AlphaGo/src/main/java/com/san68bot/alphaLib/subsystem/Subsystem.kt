package com.san68bot.alphaLib.subsystem

interface Subsystem {
    fun update()

    fun log(): String = ""
}