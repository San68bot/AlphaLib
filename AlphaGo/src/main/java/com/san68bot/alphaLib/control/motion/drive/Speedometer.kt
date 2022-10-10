package com.san68bot.alphaLib.control.motion.drive

import com.acmerobotics.dashboard.config.Config
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_angle
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_rad
import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.Point

@Config
object Speedometer {
    private var lastUpdateTime = 0.0

    var xTraveledDelta = 0.0
    var yTraveledDelta = 0.0

    private var lastAngle = 0.0
    private var angularVel = 0.0

    val radPerSec: Double
        get() = angularVel
    val degPerSec: Double
        get() = Math.toDegrees(angularVel)

    var speed: Point = Point(0.0, 0.0)
        private set

    fun update() {
        val currTime = (System.currentTimeMillis() / 1000.0)
        val dt = currTime - lastUpdateTime
        lastUpdateTime = currTime

        val xSpeed = xTraveledDelta / dt
        val ySpeed = yTraveledDelta / dt

        angularVel = (world_rad - lastAngle) / dt
        lastAngle = world_rad

        xTraveledDelta = 0.0
        yTraveledDelta = 0.0

        speed = pointDelta(Point(xSpeed, ySpeed), world_angle)
    }

    private fun pointDelta(robotDelta: Point, heading: Angle): Point {
        return Point(
            robotDelta.y * heading.sin + robotDelta.x * heading.cos,
            robotDelta.y * heading.cos - robotDelta.x * heading.sin
        )
    }
}