package com.san68bot.alphaLib.wrappers.hardware

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.geometry.TAU

class AGEncoder(private val encoder: DcMotorEx, private val ticksPerRev: Double, private val gearRatio: Double = 1.0) {
    constructor(encoder: String, ticksPerRev: Double, gearRatio: Double = 1.0, hmap: HardwareMap)
            : this(hmap.get(DcMotorEx::class.java, encoder), ticksPerRev, gearRatio)

    private var multiplier = 1.0
    private var position = 0.0
    private var lastPosition = 0.0

    val currentPos: Double
        get() {
            val pos = encoder.currentPosition.toDouble()
            position += (pos - lastPosition)
            lastPosition = pos
            return position * multiplier
        }

    val rotations: Double
        get() = currentPos / (ticksPerRev * gearRatio)
    val radians: Double
        get() = rotations * TAU

    fun reverse() { multiplier = -1.0 }

    infix fun reset(newPosition: Double) {
        lastPosition = encoder.currentPosition.toDouble()
        position = newPosition
    }
}