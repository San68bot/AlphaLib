package com.san68bot.alphaLib.subsystem.odometry

import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.control.motion.drive.Speedometer
import com.san68bot.alphaLib.control.motion.localizer.method.ThreeWheelMath
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.subsystem.Localizer
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.wrappers.hardware.AGEncoder
import kotlin.math.PI

class ThreeWheelOdometry(
    encoderConfig_LRA: ArrayList<String>,
    l_reverse: Boolean, r_reverse: Boolean, a_reverse: Boolean,
    private val lateralTrackWidth: Double,
    private val auxTrackWidth: Double,
    hmap: HardwareMap = Globals.hmap
): Localizer{
    private val encoder_ticks = 8192.0

    private val leftEncoder = AGEncoder(
        encoderConfig_LRA[0],
        encoder_ticks,
        1.0,
        hmap
    )

    private val rightEncoder = AGEncoder(
        encoderConfig_LRA[1],
        encoder_ticks,
        1.0,
        hmap
    )

    private val auxEncoder = AGEncoder(
        encoderConfig_LRA[2],
        encoder_ticks,
        1.0,
        hmap
    )

    init {
        if(l_reverse) leftEncoder.reverse()
        if(r_reverse) rightEncoder.reverse()
        if(a_reverse) auxEncoder.reverse()
    }

    private val inchesPerTick = (1.889764 * PI) / encoder_ticks

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
        Speedometer.update(inchesTravelled())
    }

    override fun inchesTravelled(): Point {
        return Point(
            ThreeWheelMath.xInchesTraveled(),
            ThreeWheelMath.yInchesTraveled()
        )
    }
}