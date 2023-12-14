package com.san68bot.alphaLib.control.motion.drive

import com.san68bot.alphaLib.control.filters.MedianFilter
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_angle_bisectedArc
import com.san68bot.alphaLib.geometry.Angle
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.toDegrees
import com.san68bot.alphaLib.utils.math.fullCircleToBisectedArc

object Speedometer {
    /**
     * Previous time for delta
     */
    private var prevTime = 0.0

    /**
     * The angular velocity
     */
    private var lastAngle = (0.0).radians
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
     * X and Y median filters to redeuce speed noise
     */
    private val xFilter = MedianFilter(3)
    private val yFilter = MedianFilter(3)

    fun objectReset() {
        prevTime = 0.0
        lastAngle = (0.0).radians
        omega = 0.0
        speed = Point.ORIGIN
        xFilter.reset_window()
        yFilter.reset_window()
    }

    /**
     * Calculates kinematics of the robot
     */
    fun update(dx: Double, dy: Double) {
        // Calculate time delta
        val currTime = (System.currentTimeMillis() / 1000.0)
        val dt = currTime - prevTime
        prevTime = currTime

        // Calculate speeds in x and y
        val xSpeed = dx / dt //xFilter.push(dx / dt).median()
        val ySpeed = dy / dt //yFilter.push(dy / dt).median()

        // Calculate angular velocity
        omega = -fullCircleToBisectedArc(global_angle_bisectedArc - lastAngle).rad / dt
        lastAngle = global_angle_bisectedArc

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