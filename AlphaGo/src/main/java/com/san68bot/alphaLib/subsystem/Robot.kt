package com.san68bot.alphaLib.subsystem

import com.san68bot.alphaLib.control.motion.localizer.GlobalPosition

interface Robot {
    val subsystems: List<Subsystem>

    val ROBOT_WIDTH: Double
    val ROBOT_LENGTH: Double

    fun setup()
    fun update()

    companion object {
        fun Localizer.setLocalizer(): Localizer {
            GlobalPosition.localizer = this
            return this
        }

        fun setLocalizer(localizer: Localizer) {
            GlobalPosition.localizer = localizer
        }
    }
}