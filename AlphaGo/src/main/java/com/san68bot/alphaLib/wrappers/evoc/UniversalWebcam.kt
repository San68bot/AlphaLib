package com.san68bot.alphaLib.wrappers.evoc

import com.san68bot.alphaLib.utils.field.Globals.hmap
import com.san68bot.alphaLib.utils.field.Globals.telemetryBuilder
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import org.openftc.easyopencv.OpenCvPipeline

class UniversalWebcam(config: String, private val orientation: OpenCvCameraRotation = OpenCvCameraRotation.UPRIGHT) {
    private val camera: OpenCvCamera

    init {
        val cameraMonitorViewId = hmap.appContext.resources.getIdentifier(
            "cameraMonitorViewId", "id", hmap.appContext.packageName
        )
        camera = OpenCvCameraFactory.getInstance().createWebcam(
            hmap.get(WebcamName::class.java, config), cameraMonitorViewId
        )
    }

    infix fun set(pipeline: OpenCvPipeline): UniversalWebcam {
        camera.setPipeline(pipeline)
        start()
        return this
    }

    private fun start(): UniversalWebcam {
        camera.openCameraDeviceAsync(object : AsyncCameraOpenListener {
            override fun onOpened() { camera.startStreaming(320, 240, orientation) }
            override fun onError(errorCode: Int) {}
        })
        telemetryBuilder.ftcDashboard.startCameraStream(camera, 30.0)
        return this
    }

    fun stop(): UniversalWebcam {
        camera.stopStreaming()
        camera.closeCameraDevice()
        telemetryBuilder.ftcDashboard.stopCameraStream()
        return this
    }
}