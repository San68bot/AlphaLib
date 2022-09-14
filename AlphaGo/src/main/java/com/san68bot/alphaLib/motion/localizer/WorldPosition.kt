package com.san68bot.alphaLib.motion.localizer

import com.san68bot.alphaLib.geometry.Pose

object WorldPosition {
    var world_pose = Pose(0.0, 0.0, 0.0)
    val world_point get() = world_pose.point

    val world_x get() = world_point.x
    val world_y get() = world_point.y

    val world_angle get() = world_pose.angle
    val world_deg get() = world_pose.deg
    val world_rad get() = world_pose.rad

    var localizer: Localizer? = null

    fun setPosition(pose: Pose) {
        world_pose = pose
        localizer?.reset(pose)
    }
}