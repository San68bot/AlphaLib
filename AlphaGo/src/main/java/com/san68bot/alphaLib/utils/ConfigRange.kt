package com.san68bot.alphaLib.utils

import com.san68bot.alphaLib.utils.math.round

class ConfigRange(@JvmField var min: Double, @JvmField var max: Double) {
    private val range get() = min..max
    infix fun contains(value: Double) = value in range
    override fun toString(): String  = "range of values between ${min round 3} and ${max round 3}"
}