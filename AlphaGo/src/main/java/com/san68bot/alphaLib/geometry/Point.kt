package com.san68bot.alphaLib.geometry

import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

data class Point(
    @JvmField var x: Double,
    @JvmField var y: Double
) {
    val hypot get()  = hypot(x, y)
    val angle get() = atan2(y, x)

    infix fun distanceTo(other: Point): Double = (other - this).hypot
    infix fun angleTo(other: Point): Angle = ((this - other).angle).radians
    infix fun angleToUnitCircle(other: Point): Angle = ((this - other).angle + PI).radians

    operator fun minus(other: Point) = Point(x - other.x, y - other.x)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun times(scaler: Double) = Point(x * scaler, y * scaler)
    fun scale(xscaler: Double, yscaler: Double) = Point(x * xscaler, y * yscaler)
    operator fun div(scaler: Double) = Point(x / scaler, y / scaler)

    companion object { val ORIGIN = Point(0.0, 0.0) }
}