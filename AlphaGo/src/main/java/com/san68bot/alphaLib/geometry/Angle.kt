package com.san68bot.alphaLib.geometry

import kotlin.math.*

data class Angle(var heading: Double, var unit: Unit) {
    enum class Unit {
        RAD, DEG
    }

    /**
     * Angle in degrees
     */
    val deg: Double
        get() = when (unit) {
            Unit.DEG -> heading
            Unit.RAD -> heading.toDegrees
        }

    /**
     * Angle in radians
     */
    val rad: Double
        get() = when (unit) {
            Unit.DEG -> heading.toRadians
            Unit.RAD -> heading
        }

    /**
     * Trig functions of the angle
     */
    val sin = sin(rad)
    val cos = cos(rad)
    val tan = tan(rad)

    companion object {
        /**
         * Creates an angle with the given heading in degrees
         */
        val Number.degrees: Angle get() = Angle(this.toDouble(), Unit.DEG)

        /**
         * Creates an angle with the given heading in radians
         */
        val Number.radians: Angle get() = Angle(this.toDouble(), Unit.RAD)
    }

    /**
     * Adds two angles
     */
    operator fun plus(other: Angle) = when (unit) {
        Unit.RAD -> (rad + other.rad).radians
        Unit.DEG -> (deg + other.deg).degrees
    }

    /**
     * Subtracts two angles
     */
    operator fun minus(other: Angle) = plus(
        when (unit) {
            Unit.RAD -> (-other.rad).radians
            Unit.DEG -> (-other.deg).degrees
        }
    )

    /**
     * Negates an angle
     */
    operator fun unaryMinus() = when (unit) {
        Unit.RAD -> (-rad).radians
        Unit.DEG -> (-deg).degrees
    }

    operator fun times(scaler: Double) = Angle(heading * scaler, unit)

    override fun toString(): String  = "deg: $deg, rad: $rad"
}

/**
 * 2PI
 */
const val TAU = (PI * 2.0)

/**
 * Converts degrees to radians
 */
val Double.toRadians get() = Math.toRadians(this)

/**
 * Converts radians to degrees
 */
val Double.toDegrees get() = Math.toDegrees(this)