package com.san68bot.alphaLib.control.motion.purepursuit

import com.san68bot.alphaLib.geometry.Point

data class CurvePoint (
    var point: Point,
    var followDistance: Double
)