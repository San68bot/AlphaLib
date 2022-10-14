package com.san68bot.alphaLib.control.logicControllers

import com.san68bot.alphaLib.utils.OneTime
import com.san68bot.alphaLib.wrappers.util.ActionTimer

class AGStateMachine(mainBlock: AGStateMachine.() -> Unit) {
    private val states = mutableListOf<AGState>()
    private var currentState = 0
    val runningState get() = states[currentState].name
    private var allStatesCompleted = false

    private val stateTimer = ActionTimer()
    private val captureTimeOneTime = OneTime()
    private var capturedTime = 0.0
    private val oneTimes = arrayListOf(OneTime(), OneTime())

    private val lastState get() = states.last()

    init {
        oneTimes.forEach { it.reset() }
        captureTimeOneTime.reset()
        states.clear()
        stateTimer.reset()
        mainBlock()
    }

    private fun resetTransition(runCustomTransition: Boolean = true) {
        if (runCustomTransition) transition.invoke()
        oneTimes.forEach { it.reset() }
        capturedTime = 0.0
        captureTimeOneTime.reset()
        resetTimer()
    }

    private fun resetTimer() {
        stateTimer.reset()
    }

    val secondsInState get() = stateTimer.seconds
    infix fun Any.checkTime(time: Double): Boolean {
        return secondsInState >= time
    }

    fun runAfterTime(time: Double, block: () -> Unit = {}): Boolean {
        if (checkTime(time)) block()
        return checkTime(time)
    }

    fun runForTime(time: Double, block: () -> Unit = {}): Boolean {
        if (secondsInState < time) block()
        return checkTime(time)
    }

    fun captureTime(): Double {
        captureTimeOneTime.once { capturedTime = stateTimer.seconds }
        return capturedTime
    }

    fun run(): Boolean {
        if (allStatesCompleted) return true
        states.forEach {
            if (it == states[currentState]) {
                oneTimes.first().once {
                    resetTimer()
                    states[currentState].enterAction?.invoke()
                }
                val exit = states[currentState].loopAction?.invoke()!!
                when {
                    exit && states[currentState].exitAction == null -> {
                        states[currentState].isCompleted = true
                        nextState()
                    }

                    exit && states[currentState].exitAction != null -> {
                        oneTimes.last().once {
                            states[currentState].exitAction?.invoke()
                            states[currentState].isCompleted = true
                        }
                        if (!allStatesCompleted) allStatesCompleted = (states[currentState] == lastState) && (!oneTimes.last().isActive())
                    }
                }
            }
        }
        return allStatesCompleted
    }

    fun state(name: String, block: AGState.() -> Unit) {
        if (states.any { it.name == name }) throw IllegalArgumentException("State with name $name already exists")
        val myState = AGState(name, block)
        states.add(myState)
        block(myState)
    }

    private var transition: (() -> Unit) = {}
    infix fun setTransitions(block: () -> Unit): AGStateMachine {
        transition = block
        return this
    }

    fun nextState(name: String, runCustomTransition: Boolean = true) {
        resetTransition(runCustomTransition)
        val nextState = states.find { it.name == name }
            ?: throw IllegalArgumentException("State with name $name does not exist")
        currentState = states.indexOf(nextState)
        resetTimer()
    }

    fun nextState(runCustomTransition: Boolean = true) {
        resetTransition(runCustomTransition)
        if (currentState == states.lastIndex) {
            allStatesCompleted = true
            states[currentState].isCompleted = true
        } else {
            currentState++
        }
        resetTimer()
    }

    fun AGState.enter(block: () -> Unit) {
        lastState.enterAction = block
    }

    fun AGState.loop(block: () -> Boolean) {
        lastState.loopAction = block
    }

    fun AGState.exit(block: () -> Unit) {
        lastState.exitAction = block
    }

    data class AGState (
        var name: String, var block: AGState.() -> Unit,
        var enterAction: (() -> Unit)? = null,
        var loopAction: (() -> Boolean)? = { true },
        var exitAction: (() -> Unit)? = null,
        var isCompleted: Boolean = false
    )
}