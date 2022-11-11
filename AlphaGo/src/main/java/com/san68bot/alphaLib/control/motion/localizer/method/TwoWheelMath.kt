package com.san68bot.alphaLib.control.motion.localizer.method

import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_angle_bisectedArc
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_point
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_pose
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.utils.math.bisectedArcToUnitCircle
import com.san68bot.alphaLib.utils.math.fullCircleToBisectedArc
import com.san68bot.alphaLib.utils.math.epsilonEquals
import kotlin.math.cos
import kotlin.math.sin

object TwoWheelMath {
    private var last_horizonal = 0.0
    private var last_vertical = 0.0

    private var angleOffset = 0.0
    private var last_angle = 0.0

    private var xInchesTraveled = 0.0
    private var yInchesTraveled = 0.0

    fun update(
        horizontalTicks: Double, verticalTicks: Double, imuAngle: Double,
        inchesPerTick: Double, xTrackWidth: Double, yTrackWidth: Double
    ) {
        val delta_x = (horizontalTicks - last_horizonal) * inchesPerTick
        val delta_y = (verticalTicks - last_vertical) * inchesPerTick
        val delta_angle = -fullCircleToBisectedArc((imuAngle - last_angle).radians)

        val final_angle = imuAngle + angleOffset

        val r_x = delta_x - (delta_angle.rad * xTrackWidth)
        val r_y = delta_y - (delta_angle.rad * yTrackWidth)

        val (sin, cos) = if (delta_angle.rad epsilonEquals 0.0) {
            1.0 - delta_angle.rad * delta_angle.rad / 6.0 to delta_angle.rad / 2.0
        } else {
            sin(delta_angle.rad) / delta_angle.rad to (1.0 - cos(delta_angle.rad)) / delta_angle.rad
        }

        val movement_x = cos * r_y + sin * r_x
        val movement_y = sin * r_y - cos * r_x

        val finalDelta = Point(
            movement_y * global_angle_bisectedArc.sin + movement_x * global_angle_bisectedArc.cos,
            movement_y * global_angle_bisectedArc.cos - movement_x * global_angle_bisectedArc.sin
        )
        global_pose = Pose(global_point + finalDelta, bisectedArcToUnitCircle(final_angle.radians))

        xInchesTraveled += r_x
        yInchesTraveled += r_y

        last_horizonal = horizontalTicks
        last_vertical = verticalTicks
        last_angle = imuAngle
    }

    fun xInchesTraveled() = xInchesTraveled
    fun yInchesTraveled() = yInchesTraveled

    fun reset(pose: Pose) {
        angleOffset = pose.rad - last_angle
    }
}