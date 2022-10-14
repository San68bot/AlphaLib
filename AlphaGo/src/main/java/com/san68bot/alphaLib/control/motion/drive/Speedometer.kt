package com.san68bot.alphaLib.control.motion.drive

import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_angle
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_rad
import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.toDegrees

object Speedometer {
    private var prevTime = 0.0

    /**
     * The X distance traveled within 1 update
     */
    private var xTraveledDelta = 0.0

    /**
     * The Y distance traveled within 1 update
     */
    private var yTraveledDelta = 0.0

    /**
     * The angular velocity
     */
    private var lastAngle = 0.0
    private var omega = 0.0

    /**
     * Angle rotated per second
     */
    val radPerSec: Double
        get() = omega
    val degPerSec: Double
        get() = omega.toDegrees

    /**
     * The speed of the robot
     */
    var speed: Point = Point.ORIGIN
        private set

    /**
     * Calculates kinematics of the robot
     */
    fun update() {
        // Calculate time delta
        val currTime = (System.currentTimeMillis() / 1000.0)
        val dt = currTime - prevTime
        prevTime = currTime

        // Calculate speeds in x and y
        val xSpeed = xTraveledDelta / dt
        val ySpeed = yTraveledDelta / dt

        // Calculate angular velocity
        omega = (world_rad - lastAngle) / dt
        lastAngle = world_rad

        // Reset traveled distance for next update
        xTraveledDelta = 0.0
        yTraveledDelta = 0.0

        // Sets speed
        speed = pointDelta(Point(xSpeed, ySpeed), world_angle)
    }

    /**
     * Adds the distance traveled to the delta and updates the class
     */
    fun update(point: Point) {
        xTraveledDelta += point.x
        yTraveledDelta += point.y
        update()
    }

    /**
     * Calculates the point delta of a point rotated by an angle
     */
    private fun pointDelta(robotDelta: Point, heading: Angle): Point {
        return Point(
            robotDelta.y * heading.sin + robotDelta.x * heading.cos,
            robotDelta.y * heading.cos - robotDelta.x * heading.sin
        )
    }
}