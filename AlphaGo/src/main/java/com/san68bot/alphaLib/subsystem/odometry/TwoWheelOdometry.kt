package com.san68bot.alphaLib.subsystem.odometry

import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.control.motion.localizer.method.TwoWheelMath
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.subsystem.Localizer
import com.san68bot.alphaLib.wrappers.hardware.AGEncoder
import com.san68bot.alphaLib.wrappers.imu.IMU
import kotlin.math.PI

class TwoWheelOdometry(
    wheelDia: Double,
    private val xTrackWidth: Double,
    private val yTrackWidth: Double,
    private val imu: IMU,
    hmap: HardwareMap
): Localizer {
    private val verticalEncoder = AGEncoder(
        "verticalEncoder",
        8192.0,
        1.0,
        hmap
    )

    private val horizontalEncoder = AGEncoder(
        "horizontalEncoder",
        8192.0,
        1.0,
        hmap
    )

    private val inchesPerTick = (wheelDia * PI) / 8192.0

    override fun reset(pose: Pose) {
        TwoWheelMath.reset(pose)
    }

    override fun update() {
        TwoWheelMath.update(
            horizontalEncoder.currentPos,
            verticalEncoder.currentPos,
            imu.firstAngle,
            inchesPerTick,
            xTrackWidth,
            yTrackWidth
        )
    }

    override fun inchesTravelled(): Point {
        return Point(
            TwoWheelMath.xInchesTraveled(),
            TwoWheelMath.yInchesTraveled()
        )
    }
}