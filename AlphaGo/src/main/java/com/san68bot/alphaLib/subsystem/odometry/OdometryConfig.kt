package com.san68bot.alphaLib.subsystem.odometry

import com.san68bot.alphaLib.geometry.Point

data class OdometryConfig(
    val config: String,
    val reverse: Boolean,
    val position: Point,
    val gear_ratio: Double = 1.0,
    val encoder_ticks: Double = 8192.0,
    val wheel_diameter: Double = 1.889764
)