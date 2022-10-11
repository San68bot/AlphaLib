package com.san68bot.alphaLib.geometry

import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import kotlin.math.cos
import kotlin.math.sin

data class Pose(
    val point: Point,
    val angle: Angle
) {
    constructor(x: Double, y: Double, theta: Double) : this(Point(x, y), theta.radians)

    /**
     * X coordinate of the pose
     */
    val x = point.x

    /**
     * Y coordinate of the pose
     */
    val y = point.y

    /**
     * Angle of the pose in degrees
     */
    val deg = angle.deg

    /**
     * Angle of the pose in radians
     */
    val rad = angle.rad

    /**
     * Hypotenuse of the pose
     */
    val distance = point.hypot

    /**
     * Calculates the heading vector of the pose
     */
    fun headingVec() = Point(cos(rad), sin(rad))

    /**
     * Returns a string representation of the object.
     */
    override fun toString(): String = "x: ${ point.x }, y: ${ point.y }, deg: ${ angle.deg }"
}