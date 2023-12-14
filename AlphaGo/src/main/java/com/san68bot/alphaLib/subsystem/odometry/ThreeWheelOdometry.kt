package com.san68bot.alphaLib.subsystem.odometry

import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.control.motion.drive.Speedometer
import com.san68bot.alphaLib.control.motion.localizer.method.ThreeWheelMath
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.subsystem.Localizer
import com.san68bot.alphaLib.utils.field.Globals
import com.san68bot.alphaLib.wrappers.hardware.AGEncoder
import kotlin.math.PI

class ThreeWheelOdometry(
    private val left_config: OdometryConfig,
    private val right_config: OdometryConfig,
    private val aux_config: OdometryConfig,
    private val lateral_track_width: Double,
    private val aux_track_width: Double,
    val hmap: HardwareMap = Globals.hmap
) : Localizer {

    private val leftEncoder = AGEncoder(
        left_config.config,
        left_config.encoder_ticks,
        left_config.gear_ratio,
        hmap = hmap
    )

    private val rightEncoder = AGEncoder(
        right_config.config,
        right_config.encoder_ticks,
        right_config.gear_ratio,
        hmap = hmap
    )

    private val auxEncoder = AGEncoder(
        aux_config.config,
        aux_config.encoder_ticks,
        aux_config.gear_ratio,
        hmap = hmap
    )

    init {
        if (left_config.reverse) leftEncoder.reverse()
        if (right_config.reverse) rightEncoder.reverse()
        if (aux_config.reverse) auxEncoder.reverse()
    }

    private val left_inches_per_tick =
        (left_config.wheel_diameter * PI) / left_config.encoder_ticks

    private val right_inches_per_tick =
        (right_config.wheel_diameter * PI) / right_config.encoder_ticks

    private val aux_inches_per_tick =
        (aux_config.wheel_diameter * PI) / aux_config.encoder_ticks

    override fun update() {
        ThreeWheelMath.update2(
            left_config.position,
            right_config.position,
            aux_config.position,
            lateral_track_width,
            aux_track_width,
            leftEncoder.currentPos * left_inches_per_tick,
            rightEncoder.currentPos * right_inches_per_tick,
            auxEncoder.currentPos * aux_inches_per_tick,
        )

        Speedometer.update(
            ThreeWheelMath.xDelta(),
            ThreeWheelMath.yDelta()
        )
    }

    override fun reset(pose: Pose) {
        ThreeWheelMath.reset(pose)
    }
}