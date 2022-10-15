package com.san68bot.alphaLib.control.motion.localizer

import com.san68bot.alphaLib.geometry.Pose
import com.san68bot.alphaLib.subsystem.Localizer
import com.san68bot.alphaLib.utils.math.unitCircleToBisectedArc

object WorldPosition {
    /**
     * Pose and point of the robot's location
     */
    var world_pose = Pose(0.0, 0.0, 0.0)
    val world_point get() = world_pose.point

    /**
     * X and Y coordinates of the robot in terms of inches
     */
    val world_x get() = world_point.x
    val world_y get() = world_point.y

    /**
     * Angle in terms of the unit circle
     */
    val world_angle get() = world_pose.angle
    val world_deg get() = world_angle.deg
    val world_rad get() = world_angle.rad

    /**
     * Angle in terms of bisected arc
     */
    val world_angle_bisectedArc get() = unitCircleToBisectedArc(world_angle)

    /**
     * Localizer that is to be used to update the world position
     */
    lateinit var localizer: Localizer

    /**
     * Sets the world position
     */
    fun setPosition(pose: Pose) {
        world_pose = pose
        localizer.reset(pose)
    }
}