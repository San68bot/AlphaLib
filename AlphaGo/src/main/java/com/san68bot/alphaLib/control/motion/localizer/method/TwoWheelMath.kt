package com.san68bot.alphaLib.control.motion.localizer.method

import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_angle_bisectedArc
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_point
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_pose
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
        val dx = (horizontalTicks - last_horizonal) * inchesPerTick
        val dy = (verticalTicks - last_vertical) * inchesPerTick
        val angle_delta = fullCircleToBisectedArc((imuAngle - last_angle).radians)

        val final_angle = imuAngle + angleOffset

        val xPrediction = angle_delta.rad * xTrackWidth
        val yPrediction = angle_delta.rad * yTrackWidth

        val rx = dx - xPrediction
        val ry = dy - yPrediction

        val (sinTerm, cosTerm) = if (angle_delta.rad epsilonEquals 0.0) {
            1.0 - angle_delta.rad * angle_delta.rad / 6.0 to angle_delta.rad / 2.0
        } else {
            sin(angle_delta.rad) / angle_delta.rad to (1.0 - cos(angle_delta.rad)) / angle_delta.rad
        }

        val x_movement = cosTerm * ry + sinTerm * rx
        val y_movement = sinTerm * ry - cosTerm * rx

        val finalDelta = Point(
            y_movement * world_angle_bisectedArc.sin + x_movement * world_angle_bisectedArc.cos,
            y_movement * world_angle_bisectedArc.cos - x_movement * world_angle_bisectedArc.sin
        )
        world_pose = Pose(world_point + finalDelta, bisectedArcToUnitCircle(final_angle.radians))

        xInchesTraveled += rx
        yInchesTraveled += ry

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