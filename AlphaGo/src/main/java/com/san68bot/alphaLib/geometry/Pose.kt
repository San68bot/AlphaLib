package com.san68bot.alphaLib.geometry

import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import kotlin.math.cos
import kotlin.math.sin

data class Pose(
    val point: Point,
    val angle: Angle
) {
    constructor(x: Double, y: Double, heading_rad: Double) : this(Point(x, y), (heading_rad).radians)

    override fun toString(): String = "x: ${ point.x }, y: ${ point.y }, deg: ${ angle.deg }"

    val x = point.x
    val y = point.y

    val deg = angle.deg
    val rad = angle.rad

    val distance = point.hypot
    fun headingVec() = Point(cos(rad), sin(rad))
}