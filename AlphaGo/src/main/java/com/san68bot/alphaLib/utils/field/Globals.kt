package com.san68bot.alphaLib.utils.field

import com.san68bot.alphaLib.control.logicControllers.AlphaGoInterface

object Globals {
    lateinit var agInterface: AlphaGoInterface

    val modeStatus get() = agInterface.modeStatus

    val hmap get() = agInterface.hardwareMap

    val telemetryBuilder get() = agInterface.telemetryBuilder

    var isAuto: Boolean = false
    var isTeleop: Boolean = false
}