package com.san68bot.alphaLib.control.motion.localizer.method

import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_angle_bisectedArc
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_point
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_pose
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.utils.math.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Localizer based on two unpowered tracking omni wheels and an orientation sensor.
 */
object TwoWheelMath {
    private var last_vertical = 0.0
    private var last_horizontal = 0.0
    private var last_imu = 0.0

    private var angleOffset = 0.0

    private var xInchesTraveled = 0.0
    private var yInchesTraveled = 0.0

    fun reset(pose: Pose) {
        angleOffset = unitCircleToBisectedArc(pose.angle).rad - last_imu
    }

    fun update(
        vertical_position: Point,
        horizontal_position: Point,
        imu_angle: Double,
        vertical_inches: Double,
        horizontal_inches: Double
    ) {
        val delta_vertical = vertical_inches - last_vertical
        val delta_horizontal = horizontal_inches - last_horizontal
        val delta_theta = fullCircleToBisectedArc((imu_angle - last_imu).radians)

        val x_arclength = delta_theta.rad * (horizontal_position.hypot * -horizontal_position.angle.rad.msign)
        val y_arclength = delta_theta.rad * (vertical_position.hypot * -vertical_position.angle.rad.msign)

        val r_x = delta_horizontal - x_arclength
        val r_y = delta_vertical - y_arclength

        val (sin, cos) = if (delta_theta.rad epsilonEquals 0.0) {
            1.0 - delta_theta.rad * delta_theta.rad / 6.0 to delta_theta.rad / 2.0
        } else {
            sin(delta_theta.rad) / delta_theta.rad to (1.0 - cos(delta_theta.rad)) / delta_theta.rad
        }

        val transformed_x = cos * r_y + sin * r_x
        val transformed_y = sin * r_y - cos * r_x
        val transformed_theta = imu_angle + angleOffset

        val finalDelta = Point(
            transformed_y * global_angle_bisectedArc.sin + transformed_x * global_angle_bisectedArc.cos,
            transformed_y * global_angle_bisectedArc.cos - transformed_x * global_angle_bisectedArc.sin
        )
        global_pose = Pose(
            global_point + finalDelta,
            bisectedArcToUnitCircle(transformed_theta.radians)
        )

        xInchesTraveled += r_x
        yInchesTraveled += r_y

        last_vertical = vertical_inches
        last_horizontal = horizontal_inches
        last_imu = imu_angle
    }

    fun xInchesTraveled() = xInchesTraveled
    fun yInchesTraveled() = yInchesTraveled
}