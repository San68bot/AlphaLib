package com.san68bot.alphaLib.utils.math

import com.san68bot.alphaLib.geometry.*
import com.san68bot.alphaLib.geometry.Angle.Companion.degrees
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Calculates and converts the angle between the two points to a unit circle angle
 */
fun unitCircleArctan(point1: Point, point2: Point = Point.ORIGIN): Angle {
    val angle = atan2(point2.y - point1.y, point2.x - point1.x)
    return (angle + PI).radians
}

/**
 * Assumes that 0 is straight up
 * Calculates and converts the angle between the two points to an angle from -180(left) to 180(right) degrees
 */
fun bisectedArcArctan(point1: Point, point2: Point = Point.ORIGIN): Angle {
    return atan2(point2.x - point1.x, point2.y - point1.y).radians
}

/**
 * Assumes that 0 is straight up
 * Converts an angle from 0 to 360 degrees to an angle from -180(left) to 180(right) degrees
 * Was wrapped()
 */
fun fullCircleToBisectedArc(angle: Angle): Angle {
    val euler = angle.rad
    val result = when {
        euler < -PI -> euler + TAU
        euler > PI -> euler - TAU
        else -> euler
    }
    return (-result).radians
}

/**
 * Assumes that 0 is straight up
 * Converts an angle from -180(left) to 180(right) degrees to an angle from 0 to 360 degrees
 */
fun bisectedArcToFullCircle(euler: Angle): Angle {
    val angle = -euler.rad
    val result = when {
        angle < 0 -> angle + TAU
        angle > TAU -> angle - TAU
        else -> angle
    }
    return result.radians
}

/**
 * Assumes that 0 is straight up
 * Mirrored version of unit circle
 * 0 -> 90, 90 -> 0, 180 -> 270, -90 or 270 -> 180
 */
fun unitCircleMirrored(angle: Angle): Angle {
    val angle_rad = -angle.rad
    val result = when {
        angle_rad < 0 -> angle_rad + TAU
        angle_rad > TAU -> angle_rad - TAU
        else -> angle_rad
    }
    return (result + PI/2.0).radians
}

/**
 * Converts a unit circle angle to an angle from -180(left) to 180(right) degrees
 */
fun unitCircleToBisectedArc(angle: Angle): Angle {
    var deg = angle.deg - 90.0
    if (deg < 0) deg += 360.0
    return fullCircleToBisectedArc(deg.degrees)
}

/**
 * Converts an angle from -180(left) to 180(right) degrees to a unit circle angle
 */
fun bisectedArcToUnitCircle(angle: Angle): Angle {
    var rad = unitCircleMirrored(angle).rad
    if (rad >= TAU) rad -= TAU
    return rad.radians
}

/**
 * Rotates a point around a pivot point by a given unit circle angle with an optional x or y offset
 */
fun rotate(pivotX: Double, pivotY: Double, angle: Angle, offset_x: Double = 0.0, offset_y: Double = 0.0): Point {
    val a0 = fullCircleToBisectedArc((PI/2.0).radians - angle).rad
    val (cos, sin) = cos(a0) to sin(a0)
    return Point(
        (offset_x) * cos - (offset_y) * sin + pivotX,
        (offset_x) * sin + (offset_y) * cos + pivotY
    )
}