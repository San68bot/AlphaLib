package com.san68bot.alphaLib.subsystem.odometry

import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.control.motion.localizer.method.ThreeWheelMath
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.subsystem.Localizer
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.wrappers.hardware.AGEncoder
import kotlin.math.PI

class ThreeWheelOdometry(
    encoderConfigLRA: ArrayList<String>,
    wheelDia: Double,
    private val lateralTrackWidth: Double,
    private val auxTrackWidth: Double,
    hmap: HardwareMap = Globals.hmap
): Localizer{
    private val leftEncoder = AGEncoder(
        encoderConfigLRA[0],
        8192.0,
        1.0,
        hmap
    )

    private val rightEncoder = AGEncoder(
        encoderConfigLRA[1],
        8192.0,
        1.0,
        hmap
    )

    private val auxEncoder = AGEncoder(
        encoderConfigLRA[2],
        8192.0,
        1.0,
        hmap
    )

    private val inchesPerTick = (wheelDia * PI) / 8192.0

    override fun reset(pose: Pose) {
        ThreeWheelMath.reset(pose)
    }

    override fun update() {
        ThreeWheelMath.update(
            leftEncoder.currentPos,
            rightEncoder.currentPos,
            auxEncoder.currentPos,
            inchesPerTick,
            lateralTrackWidth,
            auxTrackWidth
        )
    }

    override fun inchesTravelled(): Point {
        return Point(
            ThreeWheelMath.xInchesTraveled(),
            ThreeWheelMath.yInchesTraveled()
        )
    }
}