package com.san68bot.alphaLib.control.motion.drive

import com.san68bot.alphaLib.geometry.*
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

class MovementResults(private val positionError: Pose) {
    /**
     * Checks if the current distance error is within the threshold and the current angle error is within the threshold
     */
    fun check(distance: Double, angle: Angle): Boolean =
        (abs(distanceError()) <= distance) && (abs(angleError().deg) <= angle.deg)

    /**
     * Returns the current distance error
     */
    fun distanceError(): Double = sqrt((positionError.x).pow(2.0) + (positionError.y).pow(2.0))

    /**
     * Returns the current angle error
     */
    fun angleError(): Angle = positionError.angle

    /**
     * Another implementation of check() that runs a block if the errors are within the threshold
     */
    fun check(distance: Double, angle: Angle, stopDrive: Boolean = true, block: () -> Unit): Boolean {
        if (check(distance, angle)) {
            if (stopDrive) DriveMotion.stop()
            block()
        }
        return check(distance, angle)
    }

    /**
     * Returns if the current distance error is within the threshold or not
     */
    fun withinDistanceThreshold(distance: Double): Boolean = abs(distanceError()) <= distance

    /**
     * Runs a block if the distance error is within the threshold
     */
    fun atDistanceError(distance: Double, block: () -> Unit): MovementResults {
        if (withinDistanceThreshold(distance)) block()
        return this
    }

    /**
     * Returns if the current angle error is within the threshold or not
     */
    fun withinAngleThreshold(angle: Angle): Boolean = abs(angleError().deg) <= angle.deg

    /**
     * Runs a block if the angle error is within the threshold
     */
    fun atHeadingError(angle: Angle, block: () -> Unit): MovementResults {
        if (withinAngleThreshold(angle)) block()
        return this
    }
}