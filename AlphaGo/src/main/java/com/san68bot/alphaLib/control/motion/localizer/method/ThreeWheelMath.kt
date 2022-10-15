package com.san68bot.alphaLib.control.motion.localizer.method

import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_angle_bisectedArc
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_point
import com.san68bot.alphaLib.control.motion.localizer.WorldPosition.world_pose
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.utils.math.bisectedArcToUnitCircle
import com.san68bot.alphaLib.utils.math.epsilonEquals
import kotlin.math.cos
import kotlin.math.sin

object ThreeWheelMath {
    private var last_left = 0.0
    private var last_right = 0.0
    private var last_aux = 0.0

    private var angleOffset = 0.0
    private var last_angle = 0.0

    private var xInchesTraveled = 0.0
    private var yInchesTraveled = 0.0

    fun update(
        leftTicks: Double, rightTicks: Double, auxTicks: Double,
        inchesPerTick: Double, lateralTrackWidth: Double, auxTrackWidth: Double
    ) {
        val left_delta = (leftTicks - last_left) * inchesPerTick
        val right_delta = (rightTicks - last_right) * inchesPerTick
        val aux_delta = (auxTicks - last_aux) * inchesPerTick

        val angle_delta = (left_delta - right_delta) / lateralTrackWidth
        last_angle = (((leftTicks * inchesPerTick) - (rightTicks * inchesPerTick)) / lateralTrackWidth)
        val final_angle = last_angle + angleOffset

        val dx = aux_delta - (angle_delta * auxTrackWidth)
        val dy = (left_delta + right_delta) / 2.0

        val (sinTerm, cosTerm) = if (angle_delta epsilonEquals 0.0) {
            1.0 - angle_delta * angle_delta / 6.0 to angle_delta / 2.0
        } else {
            sin(angle_delta) / angle_delta to (1.0 - cos(angle_delta)) / angle_delta
        }
        val x_movement = cosTerm * dy + sinTerm * dx
        val y_movement = sinTerm * dy - cosTerm * dx

        val finalDelta = Point(
            y_movement * world_angle_bisectedArc.sin + x_movement * world_angle_bisectedArc.cos,
            y_movement * world_angle_bisectedArc.cos - x_movement * world_angle_bisectedArc.sin
        )
        world_pose = Pose(world_point + finalDelta, bisectedArcToUnitCircle(final_angle.radians))

        xInchesTraveled += dx
        yInchesTraveled += dy

        last_left = leftTicks
        last_right = rightTicks
        last_aux = auxTicks
    }

    fun xInchesTraveled() = xInchesTraveled
    fun yInchesTraveled() = yInchesTraveled

    fun reset(pose: Pose) {
        angleOffset = pose.rad - last_angle
    }
}
