package com.san68bot.alphaLib.control.motion.drive

import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_angle_bisectedArc
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_rad
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
        omega = (global_rad - lastAngle) / dt
        lastAngle = global_rad

        // Reset traveled distance for next update
        xTraveledDelta = 0.0
        yTraveledDelta = 0.0

        // Sets speed
        speed = pointDelta(xSpeed, ySpeed, global_angle_bisectedArc)
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
    private fun pointDelta(x_delta: Double, y_delta: Double, angle: Angle): Point {
        return Point(
            y_delta * angle.sin + x_delta * angle.cos,
            y_delta * angle.cos - x_delta * angle.sin
        )
    }
}