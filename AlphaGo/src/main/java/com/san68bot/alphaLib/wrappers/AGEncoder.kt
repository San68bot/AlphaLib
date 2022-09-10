package com.san68bot.alphaLib.wrappers

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.field.geometry.TAU

class AGEncoder(private val encoder: DcMotorEx, private val ticksPerRev: Double, private val gearRatio: Double = 1.0, hmap: HardwareMap) {
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