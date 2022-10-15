package com.san68bot.alphaLib.utils.math

import com.san68bot.alphaLib.geometry.*
import com.san68bot.alphaLib.geometry.Angle.Companion.degrees
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import kotlin.math.PI
import kotlin.math.atan2

/**
 * Calculates and converts the angle between the two points to a unit circle angle
 */
fun unitCircleArctan(point1: Point, point2: Point = Point.ORIGIN): Angle {
    val angle = atan2(point1.y - point2.y, point2.x - point1.x);
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
        euler < -Math.PI -> euler + TAU
        euler > Math.PI -> euler - TAU
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
fun unitCircleToBisectedArc(rad: Double): Angle {
    var deg = rad.toDegrees - 90.0
    if (deg < 0) deg += 360.0
    return fullCircleToBisectedArc(deg.degrees)
}

/**
 * Converts an angle from -180(left) to 180(right) degrees to a unit circle angle
 */
fun bisectedArcToUnitCircle(deg: Double): Angle {
    var rad = unitCircleMirrored(deg.degrees).rad
    if (rad >= TAU) rad -= TAU
    return rad.radians
}