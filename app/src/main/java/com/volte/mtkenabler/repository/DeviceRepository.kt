package com.volte.mtkenabler.repository

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.volte.mtkenabler.model.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceRepository(private val context: Context) {

    suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            device = Build.DEVICE,
            brand = Build.BRAND,
            sdkVersion = Build.VERSION.SDK_INT,
            androidVersion = Build.VERSION.RELEASE,
            chipset = getChipset(),
            isRooted = false, // Will be updated later
            isMTK = getChipset().lowercase().contains("mt") ||
                    getChipset().lowercase().contains("mediatek") ||
                    getChipset().lowercase().contains("helio")
        )
    }

    private fun getChipset(): String {
        return try {
            val hardware = Build.HARDWARE
            val board = Build.BOARD
            if (hardware.isNotEmpty()) hardware else board
        } catch (e: Exception) {
            "Unknown"
        }
    }

    suspend fun getDeviceCodeName(): String = withContext(Dispatchers.IO) {
        Build.DEVICE.lowercase()
    }

    suspend fun getAndroidSdkVersion(): Int = withContext(Dispatchers.IO) {
        Build.VERSION.SDK_INT
    }

    suspend fun getNetworkOperator(): String = withContext(Dispatchers.IO) {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.networkOperator ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun getIMSI(): String = withContext(Dispatchers.IO) {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            @Suppress("DEPRECATION")
            telephonyManager.subscriberId ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
