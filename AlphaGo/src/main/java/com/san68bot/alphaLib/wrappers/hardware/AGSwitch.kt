package com.san68bot.alphaLib.wrappers.hardware

import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.utils.field.Globals

class AGSwitch(
    config: String,
    private val invert: Boolean = false,
    hmap: HardwareMap = Globals.hmap
) {
    private val switch = hmap.get(DigitalChannel::class.java, config)

    init {
        switch.mode = DigitalChannel.Mode.INPUT
    }

    fun state() = if (invert) !switch.state else switch.state
}