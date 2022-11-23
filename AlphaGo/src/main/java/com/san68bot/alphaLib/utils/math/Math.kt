package com.san68bot.alphaLib.utils.math

import kotlin.math.*

infix fun Double.threshold(threshold: Double) =
    if (withinThreshold(threshold)) 0.0 else this

infix fun Double.withinThreshold(threshold: Double) =
    abs(this) < threshold

infix fun Double.difference(other: Double) =
    abs(this - other)

const val EPSILON = 1e-6
infix fun Double.epsilonEquals(other: Double) =
    (this difference other) < EPSILON

val Double.msign: Double
    get() = if (this < 0) -1.0 else 1.0

fun Double.isBetween(min: Double, max: Double): Boolean =
    this in min..max
fun Double.isBetweenOffset(value: Double, offset: Double): Boolean =
    isBetween(value - offset, value + offset)

infix fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun Double.notNaN() = !this.isNaN()

fun clamp(c: Double, x: Double): Double =
    if (c < -x) -x else if (c > x) x else c