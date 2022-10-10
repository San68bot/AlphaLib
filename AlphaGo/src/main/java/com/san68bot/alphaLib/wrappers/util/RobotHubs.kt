package com.san68bot.alphaLib.wrappers.util

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.HardwareMap
import com.san68bot.alphaLib.utils.field.Globals

class RobotHubs(hmap: HardwareMap = Globals.hmap) {
    private val hubs: List<LynxModule> = hmap.getAll(LynxModule::class.java)

    fun setCachingMode(mode: LynxModule.BulkCachingMode): RobotHubs {
        hubs.forEach { it.bulkCachingMode = mode }
        return this
    }

    fun clearCache(): RobotHubs {
        hubs.forEach { it.clearBulkCache() }
        return this
    }
}