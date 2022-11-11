package com.san68bot.alphaLib.subsystem.odometry

import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.control.motion.drive.Speedometer
import com.san68bot.alphaLib.control.motion.localizer.method.TwoWheelMath
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.subsystem.Localizer
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.wrappers.hardware.AGEncoder
import com.san68bot.alphaLib.wrappers.imu.IMU
import kotlin.math.PI

class TwoWheelOdometry(
    encoderConfig_VH: ArrayList<String>,
    v_reverse: Boolean, h_reverse: Boolean,
    private val xTrackWidth: Double,
    private val yTrackWidth: Double,
    private val imu: IMU,
    hmap: HardwareMap = Globals.hmap
): Localizer {
    private val encoder_ticks = 8192.0

    private val verticalEncoder = AGEncoder(
        encoderConfig_VH[0],
        encoder_ticks,
        1.0,
        hmap
    )

    private val horizontalEncoder = AGEncoder(
        encoderConfig_VH[1],
        encoder_ticks,
        1.0,
        hmap
    )

    init {
        if(v_reverse) verticalEncoder.reverse()
        if(h_reverse) horizontalEncoder.reverse()
    }

    private val inchesPerTick = (1.889764 * PI) / encoder_ticks

    override fun reset(pose: Pose) {
        TwoWheelMath.reset(pose)
    }

    override fun update() {
        TwoWheelMath.update(
            horizontalEncoder.currentPos,
            verticalEncoder.currentPos,
            -imu.firstAngle,
            inchesPerTick,
            xTrackWidth,
            yTrackWidth
        )
        Speedometer.update(inchesTravelled())
    }

    override fun inchesTravelled(): Point {
        return Point(
            TwoWheelMath.xInchesTraveled(),
            TwoWheelMath.yInchesTraveled()
        )
    }
}