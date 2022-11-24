package com.san68bot.alphaLib.control.filters

import com.san68bot.alphaLib.utils.math.clamp

/**
 * A filter that limits the rate of change of an input value.
 * Similar to a motion profile.
 * @param rate The maximum rate of change of the input value.
 * @param value The initial state of the input value.
 */
class SlewRateLimiter(
    private var rate: Double,
    private var value: Double = 0.0
) {
    private var prevTime = System.currentTimeMillis() / 1000.0

    /**
     * Filters the input to limit its rate of change.
     * @param input The input value to be limited.
     * @return The filtered value
     */
    fun calculate(input: Double): Double {
        val time = (System.currentTimeMillis() / 1000.0)
        value += clamp(
            input - value,
            rate * (time - prevTime)
        )
        prevTime = time
        return value
    }

    /**
     * Resets the rate limiter to the specified value
     * @param value The new value.
     */
    fun reset(value: Double) {
        this.value = value
        prevTime = (System.currentTimeMillis() / 1000.0)
    }

    /**
     * Sets the rate of change of the input value.
     */
    fun setRate(rate: Double) {
        this.rate = rate
    }
}