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
import kotlin.math.abs

class TwoWheelOdometry(
    private val imu: IMU,
    private val vert_encoder: OdometryConfig,
    private val horiz_encoder: OdometryConfig,
    private val hmap: HardwareMap = Globals.hmap,
    private val update2: Boolean = true
) : Localizer {
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
        if (vert_encoder.reverse) verticalEncoder.reverse()
        if (horiz_encoder.reverse) horizontalEncoder.reverse()
    }

    private val inchesPerTick = (wheel_dia * PI) / encoder_ticks
    fun verticle_inches() = verticalEncoder.currentPos * inchesPerTick
    fun horizontal_inches() = horizontalEncoder.currentPos * inchesPerTick

    private val aux_track_width = abs(vert_encoder.position.y) + abs(horiz_encoder.position.y)

    override fun update() {
        if (update2) {
            TwoWheelMath.update2(
                aux_track_width,
                -imu.yaw(),
                verticle_inches(),
                horizontal_inches()
            )
        } else {
            TwoWheelMath.update(
                vert_encoder.position,
                horiz_encoder.position,
                -imu.yaw(),
                verticalEncoder.currentPos * inchesPerTick,
                horizontalEncoder.currentPos * inchesPerTick
            )
        }

        Speedometer.update(
            TwoWheelMath.xDelta(),
            TwoWheelMath.yDelta()
        )
    }

    override fun reset(pose: Pose) {
        TwoWheelMath.reset(pose)
    }
}