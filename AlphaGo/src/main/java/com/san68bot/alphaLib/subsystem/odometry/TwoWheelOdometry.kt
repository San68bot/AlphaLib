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

data class TwoWheelConfig(val config: String, val reverse: Boolean, val position: Point)

class TwoWheelOdometry(
    private val imu: IMU,
    private val vert_encoder: TwoWheelConfig,
    private val horiz_encoder: TwoWheelConfig,
    val hmap: HardwareMap = Globals.hmap
): Localizer {
    private val encoder_ticks = 8192.0
    private val wheel_dia = 1.889764

    private val verticalEncoder = AGEncoder(
        vert_encoder.config,
        encoder_ticks,
        hmap = hmap
    )

    private val horizontalEncoder = AGEncoder(
        horiz_encoder.config,
        encoder_ticks,
        hmap = hmap
    )

    init {
        if(vert_encoder.reverse) verticalEncoder.reverse()
        if(horiz_encoder.reverse) horizontalEncoder.reverse()
    }

    private val inchesPerTick = (wheel_dia * PI) / encoder_ticks

    override fun update() {
        TwoWheelMath.update(
            vert_encoder.position,
            horiz_encoder.position,
            -imu.yaw(),
            verticalEncoder.currentPos * inchesPerTick,
            horizontalEncoder.currentPos * inchesPerTick
        )

        Speedometer.update(
            TwoWheelMath.xDelta(),
            TwoWheelMath.yDelta()
        )
    }

    override fun reset(pose: Pose) {
        TwoWheelMath.reset(pose)
    }
}