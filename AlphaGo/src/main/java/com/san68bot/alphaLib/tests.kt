package com.san68bot.alphaLib

fun main() {
    (1.0).takeIf { it == 1.0 }?.let { println("1") } ?: println("2")
}