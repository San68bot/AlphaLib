package com.san68bot.alphaLib.field

enum class Alliance {
    RED, BLUE;
    fun isRed() = this == RED
    fun isBlue() = this == BLUE
}

object RunData {
    var ALLIANCE = Alliance.RED
}