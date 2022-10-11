package com.san68bot.alphaLib.control.motion.drive

import com.san68bot.alphaLib.geometry.*
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

class MovementResults(private val positionError: Pose) {
    fun check(distance: Double, angle: Angle): Boolean = (abs(distanceError()) <= distance) && (abs(headingError().deg) <= angle.deg)
    fun distanceError(): Double = sqrt((positionError.x).pow(2.0) + (positionError.y).pow(2.0))
    fun headingError(): Angle = positionError.angle

    fun check(distance: Double, angle: Angle, stopDrive: Boolean = true, block: () -> Unit): Boolean {
        if (check(distance, angle)) {
            if (stopDrive) DriveMotion.stop()
            block()
        }
        return check(distance, angle)
    }

    fun atDistanceError(distance: Double, block: () -> Unit): MovementResults {
        if (abs(distanceError()) <= distance) block()
        return this
    }

    fun atHeadingError(angle: Angle, block: () -> Unit): MovementResults {
        if (abs(headingError().deg) <= angle.deg) block()
        return this
    }
}