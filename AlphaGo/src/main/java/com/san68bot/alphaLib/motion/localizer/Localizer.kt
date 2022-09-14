package com.san68bot.alphaLib.motion.localizer

import com.san68bot.alphaLib.geometry.Pose

abstract class Localizer {
    open fun update() {}
    open fun reset(pose: Pose) {}
}