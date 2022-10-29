package com.san68bot.alphaLib.wrappers.hardware

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.geometry.TAU
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.utils.math.unitCircleMirrored

class AGEncoder(
    private val encoder: DcMotorEx,
    private val ticksPerRev: Double,
    private val gearRatio: Double = 1.0
) {
    constructor(
        encoder: String,
        ticksPerRev: Double,
        gearRatio: Double = 1.0,
        hmap: HardwareMap = Globals.hmap
    ): this(hmap.get(DcMotorEx::class.java, encoder), ticksPerRev, gearRatio)

    private var multiplier = 1.0
    private var position = 0.0
    private var lastPosition = 0.0

    /**
     * Current position of the encoder
     */
    val currentPos: Double
        get() {
            val pos = encoder.currentPosition.toDouble()
            position += (pos - lastPosition)
            lastPosition = pos
            return position * multiplier
        }

    /**
     * Number of rotations the encoder has made
     */
    val rotations: Double
        get() = currentPos / (ticksPerRev * gearRatio)

    /**
     * Current angle of the encoder
     */
    val angle: Angle
        get() = (rotations * TAU).radians

    /**
     * Current angle of the encoder in the unit circle
     */
    val unitCircleAngle: Angle
        get() = unitCircleMirrored(angle)

    fun reverse() { multiplier = -1.0 }

    infix fun reset(newPosition: Double) {
        lastPosition = encoder.currentPosition.toDouble()
        position = newPosition
    }
}