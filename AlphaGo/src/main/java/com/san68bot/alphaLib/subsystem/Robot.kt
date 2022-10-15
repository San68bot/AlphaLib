package com.san68bot.alphaLib.subsystem

import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition

interface Robot {
    val subsystems: List<Subsystem>

    val ROBOT_WIDTH: Double
    val ROBOT_LENGTH: Double

    fun setup()

    fun update()

    companion object {
        fun setLocalizer(localizer: Localizer) {
            GlobalPosition.localizer = localizer
        }
    }
}