package com.san68bot.alphaLib.geometry

import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.utils.math.round
import com.san68bot.alphaLib.utils.math.unitCircleArctan
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

data class Point(
    @JvmField var x: Double,
    @JvmField var y: Double
) {
    /**
     * Calculates the hypotenuse of the point
     */
    val hypot get()  = hypot(x, y)

    /**
     * Calculates the angle of the point
     */
    val angle get() = atan2(y, x).radians

    /**
     * Calculates the distance between two points
     */
    infix fun distanceTo(other: Point): Double = (other - this).hypot

    /**
     * Calculates the angle between two points
     */
    infix fun angleTo(other: Point): Angle = (this - other).angle

    /**
     * Calculates the angle between two points in terms of the unit circle
     */
    infix fun angleTo_UnitCircle(other: Point): Angle = unitCircleArctan(other, this)

    /**
     * Subtracts two points
     */
    operator fun minus(other: Point) =
        Point(x - other.x, y - other.y)

    /**
     * Adds two points
     */
    operator fun plus(other: Point) =
        Point(x + other.x, y + other.y)

    /**
     * Scales a point by a scaler value
     */
    operator fun times(scaler: Double) =
        Point(x * scaler, y * scaler)

    /**
     * Scales a point by an x scaler and y scaler value
     */
    fun scale(xscaler: Double, yscaler: Double) =
        Point(x * xscaler, y * yscaler)

    /**
     * Divides a point
     */
    operator fun div(scaler: Double) =
        Point(x / scaler, y / scaler)

    /**
     * String representation of the point
     */
    override fun toString(): String = "(${x round 3}, ${y round 3})"

    /**
     * The origin point
     */
    companion object { val ORIGIN = Point(0.0, 0.0) }
}