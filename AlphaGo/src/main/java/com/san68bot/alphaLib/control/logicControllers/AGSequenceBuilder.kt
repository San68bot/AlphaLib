package com.san68bot.alphaLib.control.logicControllers

import com.san68bot.alphaLib.utils.OneTime
import com.san68bot.alphaLib.wrappers.util.ActionTimer

/**
 * Robust State Machine Builder creates with Kotlin DSLs
 */
class AGSequenceBuilder(
    mainBlock: AGSequenceBuilder.() -> Unit
) {
    /**
     * List of all sequences
     */
    private val sequences = mutableListOf<AGSequence>()

    /**
     * Current running sequence
     */
    private var currentSequence = 0

    /**
     * Sequence currently active
     */
    private val runningSequence get() = sequences[currentSequence]

    /**
     * The last sequence of the sequence machine
     */
    private lateinit var lastSequence: AGSequence

    /**
     * Sequence timer, resets on state change
     */
    private val sequenceTimer = ActionTimer()

    /**
     * Retrieves instantaneous time in seconds
     */
    private val captureTimeOneTime = OneTime()

    /**
     * Captured time in seconds
     */
    private var capturedTime = 0.0

    /**
     * List of one time actions
     */
    private val oneTimes = arrayListOf(OneTime(), OneTime())

    init {
        sequences.clear()
        oneTimes.forEach { it.reset() }
        captureTimeOneTime.reset()
        sequenceTimer.reset()
        mainBlock()
    }

    fun refresh() {
        currentSequence = 0
        oneTimes.forEach { it.reset() }
        captureTimeOneTime.reset()
        sequenceTimer.reset()
    }

    /**
     * Adds a sequence to the list of sequences
     */
    fun sequence(name: String, block: AGSequence.() -> Unit) {
        if (sequences.any { it.name == name })
            throw IllegalArgumentException("Sequence name $name already exists")
        val mySequence = AGSequence(name, block)
        sequences.add(mySequence)
        lastSequence = mySequence
        block(mySequence)
    }

    /**
     * Enter block of code when sequence is entered
     */
    fun AGSequence.enter(block: () -> Unit) {
        lastSequence.enterAction = block
    }

    /**
     * Loop block of code when sequence is running
     */
    fun AGSequence.loop(block: () -> Boolean) {
        lastSequence.loopAction = block
    }

    /**
     * Exit block of code when sequence is exited
     */
    fun AGSequence.exit(block: () -> Unit) {
        lastSequence.exitAction = block
    }

    /**
     * Runs the sequence builder
     */
    fun run() {
        runningSequence.apply {
            oneTimes[0].once {
                resetTimer()
                enterAction?.invoke()
            }
            loopAction?.invoke()!!.takeIf { bool -> bool }?.let {
                oneTimes[1].once {
                    exitAction?.invoke()
                }
                if (this != lastSequence) nextSequence()
            }
        }
    }

    /**
     * Changes the current sequence to the next sequence
     */
    fun nextSequence(name: String? = null) {
        if (currentSequence == sequences.lastIndex) return
        if (name != null) {
            sequences.find { it.name == name }?.let {
                currentSequence = sequences.indexOf(it)
            } ?: throw IllegalArgumentException("Sequence with name $name does not exist")
        } else {
            currentSequence++
        }
        oneTimes.forEach { it.reset() }
    }

    /**
     * Retrieves the time in seconds that the state has been running
     */
    val secondsInState get() = sequenceTimer.seconds

    /**
     * Resets the state timer
     */
    private fun resetTimer() {
        sequenceTimer.reset()
    }

    /**
     * Checks if the time in seconds has been reached
     * @param time Time in seconds
     * @return True if time has been reached
     */
    infix fun Any.checkTime(time: Double): Boolean {
        return secondsInState >= time
    }

    /**
     * Runs a block of code after a certain time
     * @param time Time in seconds
     * @param block Block of code to run
     * @return True if time has been reached
     */
    fun runAfterTime(time: Double, block: () -> Unit = {}): Boolean {
        val result = checkTime(time)
        if (result) block()
        return result
    }

    /**
     * Runs a block of code for a certain duration of time
     * @param time Time in seconds
     * @param block Block of code to run
     * @return True if time has been reached
     */
    fun runForTime(time: Double, block: () -> Unit = {}): Boolean {
        if (secondsInState < time) block()
        return checkTime(time)
    }

    /**
     * Captures the time in seconds that the state has been running
     * @return Time in seconds
     */
    fun captureTime(): Double {
        captureTimeOneTime.once { capturedTime = sequenceTimer.seconds }
        return capturedTime
    }

    /**
     * Data class that defines properties of a sequence
     */
    data class AGSequence(
        val name: String, val block: AGSequence.() -> Unit,
        var enterAction: (() -> Unit)? = null,
        var loopAction: (() -> Boolean)? = { true },
        var exitAction: (() -> Unit)? = null,
    )
}