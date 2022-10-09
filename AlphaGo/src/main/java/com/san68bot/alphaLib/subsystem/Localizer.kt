package com.san68bot.alphaLib.subsystem

import com.san68bot.alphaLib.geometry.Point
import com.san68bot.alphaLib.geometry.Pose

interface Localizer {
    fun update()

    fun reset(pose: Pose)

    fun inchesTravelled(): Point
}