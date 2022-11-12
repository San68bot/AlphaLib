package com.san68bot.alphaLib.control.motion.drive

import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_angle_bisectedArc
import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.toDegrees

object Speedometer {
    /**
     * Previous time for delta
     */
    private var prevTime = 0.0

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
    fun update(dx: Double, dy: Double) {
        // Calculate time delta
        val currTime = (System.currentTimeMillis() / 1000.0)
        val dt = currTime - prevTime
        prevTime = currTime

        // Calculate speeds in x and y
        val xSpeed = dx / dt
        val ySpeed = dy / dt

        // Calculate angular velocity
        omega = (global_angle_bisectedArc.rad - lastAngle) / dt
        lastAngle = global_angle_bisectedArc.rad

        // Sets speed
        speed = pointDelta(
            xSpeed, ySpeed, global_angle_bisectedArc
        )
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