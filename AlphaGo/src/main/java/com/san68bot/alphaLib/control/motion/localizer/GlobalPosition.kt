package com.san68bot.alphaLib.control.motion.localizer

import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.subsystem.Localizer
import com.san68bot.alphaLib.utils.math.unitCircleToBisectedArc

object GlobalPosition {
    /**
     * Pose and point of the robot's location
     */
    var global_pose = Pose(0.0, 0.0, 0.0)
    val global_point get() = global_pose.point

    /**
     * X and Y coordinates of the robot in terms of inches
     */
    val global_x get() = global_point.x
    val global_y get() = global_point.y

    /**
     * Angle in terms of the unit circle
     */
    val global_angle get() = global_pose.angle
    val global_deg get() = global_angle.deg
    val global_rad get() = global_angle.rad

    /**
     * Angle in terms of bisected arc
     */
    val global_angle_bisectedArc get() = unitCircleToBisectedArc(global_angle)

    /**
     * Localizer that is to be used to update the world position
     */
    lateinit var localizer: Localizer

    /**
     * Sets the world position
     */
    fun setPosition(pose: Pose) {
        global_pose = pose
        if (this::localizer.isInitialized) localizer.reset(pose)
    }
}