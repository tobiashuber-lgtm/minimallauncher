package com.minimal.launcher

import android.content.Context
import android.hardware.camera2.CameraManager

object FlashlightHelper {

    private var isOn = false

    fun toggle(context: Context) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return
            isOn = !isOn
            cameraManager.setTorchMode(cameraId, isOn)
        } catch (e: Exception) {
            // Kein Blitz/Taschenlampe auf diesem Geraet verfuegbar - bewusst ignoriert
        }
    }
}
