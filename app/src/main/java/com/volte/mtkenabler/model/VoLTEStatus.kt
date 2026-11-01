package com.volte.mtkenabler.model

enum class VoLTEStatus {
    ENABLED,
    DISABLED,
    UNKNOWN,
    ERROR
}

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val device: String,
    val brand: String,
    val sdkVersion: Int,
    val androidVersion: String,
    val chipset: String,
    val isRooted: Boolean,
    val isMTK: Boolean
)

data class VoLTEConfig(
    val enableVoLTE: Boolean = true,
    val enableVoWiFi: Boolean = true,
    val enableVideoCalling: Boolean = true,
    val carrierName: String = "",
    val mcc: String = "",
    val mnc: String = ""
)

data class OperationResult(
    val success: Boolean,
    val message: String,
    val details: String = ""
)
