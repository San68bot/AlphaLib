package com.san68bot.alphaLib.control.logicControllers

import com.san68bot.alphaLib.utils.OneTime
import com.san68bot.alphaLib.wrappers.util.ActionTimer

/**
 * Robust State Machine Builder creates with Kotlin DSLs
 */
class AGStateMachine(
    mainBlock: AGStateMachine.() -> Unit
) {
    /**
     * List of all states
     */
    private val states = mutableListOf<AGState>()

    /**
     * Current running state
     */
    private var currentState = 0

    /**
     * State currently active
     */
    private val runningState get() = states[currentState]

    /**
     * The last state of the state machine
     */
    private lateinit var lastState: AGState

    /**
     * Variable to check if all states have been completed
     */
    var allStatesCompleted = false

    /**
     * State timer, resets on state change
     */
    private val stateTimer = ActionTimer()

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

    /**
     * Resets all variables to default and runs the main block
     */
    init {
        states.clear()
        oneTimes.forEach { it.reset() }
        captureTimeOneTime.reset()
        stateTimer.reset()
        mainBlock()
    }

    /**
     * Creates a new state
     */
    fun state(name: String, block: AGState.() -> Unit) {
        if (states.any { it.name == name })
            throw IllegalArgumentException("State with name $name already exists")
        val myState = AGState(name, block)
        states.add(myState)
        lastState = myState
        block(myState)
    }

    /**
     * Enter block of code when state is entered
     */
    fun AGState.enter(block: () -> Unit) {
        lastState.enterAction = block
    }

    /**
     * Loop block of code while state is active
     */
    fun AGState.loop(block: () -> Boolean) {
        lastState.loopAction = block
    }

    /**
     * Exit block of code when state is exited
     */
    fun AGState.exit(block: () -> Unit) {
        lastState.exitAction = block
    }

    /**
     * Runs the state machine
     * @return True if all states have been completed
     */
    fun run(): Boolean {
        if (allStatesCompleted) return true
        runningState.apply {
            oneTimes[0].once {
                resetTimer()
                enterAction?.invoke()
            }
            loopAction?.invoke()!!.takeIf { bool -> bool }?.let {
                oneTimes[1].once {
                    exitAction?.invoke()
                }
                allStatesCompleted = (this == lastState) && (!oneTimes[1].isActive())
                nextState()
            }
        }
        return allStatesCompleted
    }

    /**
     * Resets the state machine for a new run
     */
    fun refresh() {
        allStatesCompleted = false

        oneTimes.forEach { it.reset() }
        captureTimeOneTime.reset()

        currentState = 0

        capturedTime = 0.0
        resetTimer()
    }

    /**
     * Goes to the next state
     * @param name Name of the state to go to
     * @param runCustomTransition Whether to run the custom transition
     */
    fun nextState(name: String, runCustomTransition: Boolean = true) {
        resetTransition(runCustomTransition)
        val nextState = states.find { it.name == name }
            ?: throw IllegalArgumentException("State with name $name does not exist")
        currentState = states.indexOf(nextState) - 1
        resetTimer()
    }

    /**
     * Goes to the next state, with option to run custom transition
     * @param runCustomTransition Whether to run the custom transition
     */
    fun nextState(runCustomTransition: Boolean = true) {
        resetTransition(runCustomTransition)
        if (currentState == states.lastIndex) {
            allStatesCompleted = true
        } else {
            currentState++
        }
        resetTimer()
    }

    /**
     * Custom transition that can be run each time state changes
     */
    private var transition: (() -> Unit) = {}
    infix fun setTransitions(block: () -> Unit): AGStateMachine {
        transition = block
        return this
    }

    /**
     * Custom transition that can be run each time state changes
     */
    private fun resetTransition(runCustomTransition: Boolean = true) {
        if (runCustomTransition) transition.invoke()
        oneTimes.forEach { it.reset() }
        capturedTime = 0.0
        captureTimeOneTime.reset()
        resetTimer()
    }

    /**
     * Retrieves the time in seconds that the state has been running
     */
    val secondsInState get() = stateTimer.seconds

    /**
     * Resets the state timer
     */
    private fun resetTimer() {
        stateTimer.reset()
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
        captureTimeOneTime.once { capturedTime = stateTimer.seconds }
        return capturedTime
    }

    /**
     * Data class that defines properties of a state
     */
    data class AGState (
        var name: String, var block: AGState.() -> Unit,
        var enterAction: (() -> Unit)? = null,
        var loopAction: (() -> Boolean)? = { true },
        var exitAction: (() -> Unit)? = null
    )
}
