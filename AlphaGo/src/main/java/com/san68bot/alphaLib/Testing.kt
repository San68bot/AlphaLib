package com.san68bot.alphaLib

fun main() {
    val x = maxOf(1, 2).let { if (it == 2) {
        println("x is 2")
        2
    } else {
        println("x is not 2")
        1
    }
    }
}