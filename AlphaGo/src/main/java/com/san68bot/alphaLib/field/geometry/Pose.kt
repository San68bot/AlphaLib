package com.san68bot.alphaLib.field.geometry

import kotlin.math.cos
import kotlin.math.sin

data class Pose(
    val point: Point,
    val heading: Angle
) {
    constructor(x: Double, y: Double, heading_rad: Double) : this(Point(x, y), Angle.createUnwrappedRad(heading_rad))
    constructor(x: Double, y: Double, heading_rad: Double, wrap: Any) : this(Point(x, y), Angle.createWrappedRad(heading_rad))

    override fun toString(): String = "x: ${ point.x }, y: ${ point.y }, deg: ${ heading.deg }"

    val x = point.x
    val y = point.y

    val deg = heading.deg
    val rad = heading.rad

    val distance = point.hypot
    fun headingVec() = Point(cos(rad), sin(rad))
}