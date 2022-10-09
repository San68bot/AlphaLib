package com.san68bot.alphaLib.utils.math

import com.san68bot.alphaLib.geometry.*
import kotlin.math.PI
import kotlin.math.atan2

fun unitCircleArctan(point1: Point, point2: Point = Point.ORIGIN): Angle {
    val angle = atan2(point1.y - point2.y, point2.x - point1.x);
    return Angle(angle + Math.PI, Angle.Unit.RAD)
}

fun angleToEuler(angle: Angle): Angle {
    val euler = angle.rad
    val result = when {
        euler < -Math.PI -> euler + TAU
        euler > Math.PI -> euler - TAU
        else -> euler
    }
    return Angle(result, Angle.Unit.RAD)
}

fun eulerToAngle(euler: Angle): Angle {
    val angle = euler.rad
    val result = when {
        angle < 0 -> angle + TAU
        angle > TAU -> angle - TAU
        else -> angle
    }
    return Angle(result, Angle.Unit.RAD)
}

fun eulerToAngleModified(euler: Angle): Angle {
    val angle = -euler.rad
    val result = when {
        angle < 0 -> angle + TAU
        angle > TAU -> angle - TAU
        else -> angle
    }
    return Angle(result + PI/2.0, Angle.Unit.RAD)
}

fun unitCircleToHalfCircle(pose: Pose): Pose {
    return Pose(Point(pose.x, pose.y), Angle(-angleToEuler(Angle(unitCircleToHalfCircle(pose.rad), Angle.Unit.DEG)).deg, Angle.Unit.DEG))
}

fun unitCircleToHalfCircle(rad: Double): Double {
    var deg = rad.toDegrees - 90.0
    if (deg < 0) deg += 360.0
    return -angleToEuler(Angle(deg, Angle.Unit.DEG)).deg
}

fun halfCircleToUnitCircle(pose: Pose): Pose {
    return Pose(Point(pose.x, pose.y), Angle(halfCircleToUnitCircle(pose.rad), Angle.Unit.RAD))
}

fun halfCircleToUnitCircle(deg: Double): Double {
    var rad = eulerToAngleModified(Angle(deg, Angle.Unit.DEG)).rad
    if (rad >= 2* PI) rad -= 2* PI
    return rad
}

fun angleBetween_deg(point: Point, otherPoint: Point): Double {
    return atan2(otherPoint.x - point.x, otherPoint.y - point.y).toDegrees
}

fun angleWrap_deg(_angle: Double): Double {
    var angle = _angle
    while (angle < -180.0)
        angle += 360.0
    while (angle > 180.0)
        angle -= 360.0
    return angle
}