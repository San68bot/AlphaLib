package com.san68bot.alphaLib.control.motion.localizer.method

import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_angle_bisectedArc
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_point
import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition.global_pose
import com.san68bot.alphaLib.geometry.Angle.Companion.radians
import com.san68bot.alphaLib.utils.math.bisectedArcToUnitCircle
import com.san68bot.alphaLib.utils.math.epsilonEquals
import com.san68bot.alphaLib.utils.math.unitCircleToBisectedArc
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

/**
 * Localizer based on three unpowered tracking omni wheels.
 */
object ThreeWheelMath {
    private var last_left = 0.0
    private var last_right = 0.0
    private var last_aux = 0.0

    private var angleOffset = 0.0
    private var last_angle = 0.0

    var xInchesTraveled = 0.0
    var yInchesTraveled = 0.0

    private var r_x = 0.0
    private var r_y = 0.0

    fun objectReset() {
        last_left = 0.0
        last_right = 0.0
        last_aux = 0.0
        last_angle = 0.0
        angleOffset = 0.0
        xInchesTraveled = 0.0
        yInchesTraveled = 0.0
        r_x = 0.0
        r_y = 0.0
    }

    fun reset(pose: Pose) {
        angleOffset = unitCircleToBisectedArc(pose.angle).rad - last_angle
    }

    fun update2(
        left_position: Point,
        right_position: Point,
        aux_position: Point,
        lateral_track_width: Double,
        aux_track_width: Double,
        left_inches: Double,
        right_inches: Double,
        aux_inches: Double
    ) {
        val left_delta = left_inches - last_left
        val right_delta = right_inches - last_right
        val aux_delta = aux_inches - last_aux

        val angle_delta = (left_delta - right_delta) / lateral_track_width
        last_angle = (left_inches - right_inches) / lateral_track_width
        val final_angle = last_angle + angleOffset

        r_x = aux_delta - (angle_delta * aux_track_width)
        r_y = (left_delta + right_delta) / 2.0

        val (sinTerm, cosTerm) = if (angle_delta epsilonEquals 0.0) {
            1.0 - angle_delta * angle_delta / 6.0 to angle_delta / 2.0
        } else {
            sin(angle_delta) / angle_delta to (1.0 - cos(angle_delta)) / angle_delta
        }

        val x_movement = cosTerm * r_y + sinTerm * r_x
        val y_movement = sinTerm * r_y - cosTerm * r_x

        val finalDelta = Point(
            y_movement * global_angle_bisectedArc.sin + x_movement * global_angle_bisectedArc.cos,
            y_movement * global_angle_bisectedArc.cos - x_movement * global_angle_bisectedArc.sin
        )

        global_pose = Pose(
            global_point + finalDelta,
            bisectedArcToUnitCircle(final_angle.radians)
        )

        xInchesTraveled += r_x
        yInchesTraveled += r_y

        last_left = left_inches
        last_right = right_inches
        last_aux = aux_inches
    }

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

        r_x = aux_delta - (angle_delta * auxTrackWidth)
        r_y = (left_delta + right_delta) / 2.0

        val (sinTerm, cosTerm) = if (angle_delta epsilonEquals 0.0) {
            1.0 - angle_delta * angle_delta / 6.0 to angle_delta / 2.0
        } else {
            sin(angle_delta) / angle_delta to (1.0 - cos(angle_delta)) / angle_delta
        }
        val x_movement = cosTerm * r_y + sinTerm * r_x
        val y_movement = sinTerm * r_y - cosTerm * r_x

        val finalDelta = Point(
            y_movement * global_angle_bisectedArc.sin + x_movement * global_angle_bisectedArc.cos,
            y_movement * global_angle_bisectedArc.cos - x_movement * global_angle_bisectedArc.sin
        )
        global_pose = Pose(global_point + finalDelta, bisectedArcToUnitCircle(final_angle.radians))

        xInchesTraveled += r_x
        yInchesTraveled += r_y

        last_left = leftTicks
        last_right = rightTicks
        last_aux = auxTicks
    }

    fun xDelta() = r_x
    fun yDelta() = r_y
}
