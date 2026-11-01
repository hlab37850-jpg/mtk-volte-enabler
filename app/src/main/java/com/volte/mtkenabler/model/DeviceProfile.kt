package com.volte.mtkenabler.model

data class DeviceProfile(
    val deviceCodeName: String,
    val deviceName: String,
    val chipset: String,
    val androidVersions: List<Int>,
    val volteSupport: Boolean,
    val mbnPath: String?,
    val buildPropPath: String,
    val vendorPath: String,
    val carrierConfigPath: String?
) {
    companion object {
        val LANCELOT = DeviceProfile(
            deviceCodeName = "lancelot",
            deviceName = "Redmi 9",
            chipset = "MediaTek Helio G85 (MT6769V/CB)",
            androidVersions = listOf(9, 10, 11, 12, 13, 14, 15, 16, 17),
            volteSupport = true,
            mbnPath = "/vendor/firmware/mbn",
            buildPropPath = "/system/build.prop",
            vendorPath = "/vendor",
            carrierConfigPath = "/vendor/etc/carrierconfig"
        )

        val SHIVA = DeviceProfile(
            deviceCodeName = "shiva",
            deviceName = "Redmi 9A",
            chipset = "MediaTek Helio G25",
            androidVersions = listOf(10, 11, 12),
            volteSupport = true,
            mbnPath = "/vendor/firmware/mbn",
            buildPropPath = "/system/build.prop",
            vendorPath = "/vendor",
            carrierConfigPath = "/vendor/etc/carrierconfig"
        )

        fun getProfileByCodename(codename: String): DeviceProfile? {
            return when (codename.lowercase()) {
                "lancelot" -> LANCELOT
                "shiva" -> SHIVA
                else -> null
            }
        }
    }
}
