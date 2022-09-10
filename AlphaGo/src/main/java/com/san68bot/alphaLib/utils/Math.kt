package com.san68bot.alphaLib.utils

import com.qualcomm.robotcore.util.Range
import com.san68bot.alphaLib.field.geometry.Angle
import com.san68bot.alphaLib.field.geometry.Point
import kotlin.math.absoluteValue
import kotlin.math.atan2

infix fun Double.clip(range:Double) = Range.clip(this, -range, range)
infix fun Double.threshold(threshold: Double) = if (this.absoluteValue < threshold) 0.0 else this
infix fun Double.difference(other: Double) = (this - other).absoluteValue

const val pi = Math.PI

const val EPSILON = 1e-6
infix fun Double.epsilonEquals(other: Double) = this difference other < EPSILON

fun avg(vararg nums: Double): Double = nums.average()
fun sum(vararg nums: Double): Double = nums.sum()

fun Double.isBetween(min: Double, max: Double): Boolean = this in min..max
fun Double.isBetweenOffset(value: Double, offset: Double): Boolean = isBetween(value - offset, value + offset)

infix fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

fun Double.notNaN() = !this.isNaN()

fun unitCircleArctan(point1: Point, point2: Point = Point.ORIGIN): Angle {
    val angle = atan2(point1.y - point2.y, point2.x - point1.x);
    return Angle(angle + Math.PI, Angle.Unit.RAD)
}