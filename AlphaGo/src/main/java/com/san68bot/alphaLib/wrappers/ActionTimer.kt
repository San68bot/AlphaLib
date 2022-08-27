package com.san68bot.alphaLib.wrappers

import com.qualcomm.robotcore.util.ElapsedTime

class ActionTimer {
    private val timer = ElapsedTime()

    val seconds: Double
        get() = timer.seconds()
    val milliseconds: Double
        get() = timer.milliseconds()

    infix fun checkTime(sec: Double) = seconds > sec
    infix fun checkTimeMillis(millis: Double) = milliseconds > millis
    fun reset() = timer.reset()
}