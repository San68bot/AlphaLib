package com.san68bot.alphaLib.control.logicControllers

class Sequential(private vararg val commands: () -> Unit) {
    private var counter = 1
    private var iterator: () -> Boolean = { false }
    private val size = commands.size

    fun reset() {
        counter = 1
    }

    fun setIterator(iterator: () -> Boolean): Sequential {
        this.iterator = iterator
        return this
    }

    fun update() {
        commands[counter-1].invoke()
        if (iterator.invoke()) {
            counter++
            if (counter > size) counter = 1
        }
    }
}