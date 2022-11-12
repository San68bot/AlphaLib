package com.san68bot.alphaLib.subsystem

import com.san68bot.alphaLib.geometry.Pose

interface Localizer {
    fun update()
    fun reset(pose: Pose)
}