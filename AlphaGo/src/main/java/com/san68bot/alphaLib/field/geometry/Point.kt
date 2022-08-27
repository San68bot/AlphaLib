package com.san68bot.alphaLib.field.geometry

import kotlin.math.hypot

data class Point(
    @JvmField var x: Double,
    @JvmField var y: Double
) {
    val hypot get()  = hypot(x, y)

    infix fun distanceTo(other: Point): Double = (other - this).hypot
    fun closestPoint(firstPoint: Point, vararg additionalPoints: Point) = additionalPoints.fold(firstPoint) { result, next ->
        if (distanceTo(next) < distanceTo(result)) next else result
    }

    operator fun minus(other: Point) = Point(x - other.x, y - other.x)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun times(scaler: Double) = Point(x * scaler, y * scaler)
    fun scale(xscaler: Double, yscaler: Double) = Point(x * xscaler, y * yscaler)
    operator fun div(scaler: Double) = Point(x / scaler, y / scaler)

    companion object {
        val ORIGIN = Point(0.0, 0.0)
    }
}