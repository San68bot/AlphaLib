package com.san68bot.alphaLib.control.motion.drive

import com.san68bot.alphaLib.geometry.*
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

class MovementResults(val positionError: Pose) {
    fun check(distance: Double, deg: Angle): Boolean = distanceError().absoluteValue <= distance && headingError().deg.absoluteValue <= deg.deg
    fun distanceError(): Double { return sqrt((positionError.x).pow(2.0) + (positionError.y).pow(2.0))  }
    fun headingError(): Angle { return positionError.angle }

    fun check(distance: Double, deg: Angle, stopDrive: Boolean = true, block: () -> Unit): Boolean {
        if (check(distance, deg)) {
            if (stopDrive) DriveMotion.stop()
            block()
        }
        return check(distance, deg)
    }

    fun atDistanceError(distance: Double, block: () -> Unit): MovementResults {
        if (distanceError().absoluteValue <= distance) block()
        return this
    }

    fun atHeadingError(deg: Angle, block: () -> Unit): MovementResults {
        if (headingError().deg.absoluteValue <= deg.deg) block()
        return this
    }
}