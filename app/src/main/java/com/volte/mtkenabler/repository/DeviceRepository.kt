package com.volte.mtkenabler.repository

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.volte.mtkenabler.model.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceRepository(private val context: Context) {
    private val TAG = "DeviceRepository"

    suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            
            DeviceInfo(
                manufacturer = Build.MANUFACTURER ?: "Unknown",
                model = Build.MODEL ?: "Unknown",
                device = Build.DEVICE ?: "Unknown",
                brand = Build.BRAND ?: "Unknown",
                sdkVersion = Build.VERSION.SDK_INT,
                androidVersion = Build.VERSION.RELEASE ?: "Unknown",
                chipset = getChipset(),
                isRooted = false,
                isMTK = isMTKDevice()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device info", e)
            DeviceInfo(
                manufacturer = "Unknown",
                model = "Unknown",
                device = "Unknown",
                brand = "Unknown",
                sdkVersion = Build.VERSION.SDK_INT,
                androidVersion = Build.VERSION.RELEASE ?: "Unknown",
                chipset = "Unknown",
                isRooted = false,
                isMTK = false
            )
        }
    }

    private fun getChipset(): String {
        return try {
            val hardware = Build.HARDWARE ?: ""
            val board = Build.BOARD ?: ""
            if (hardware.isNotEmpty()) hardware else board.ifEmpty { "Unknown" }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chipset", e)
            "Unknown"
        }
    }

    private fun isMTKDevice(): Boolean {
        return try {
            val chipset = getChipset().lowercase()
            chipset.contains("mt") || 
            chipset.contains("mediatek") || 
            chipset.contains("helio") ||
            chipset.contains("dimensity")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking MTK device", e)
            false
        }
    }

    suspend fun getDeviceCodeName(): String = withContext(Dispatchers.IO) {
        try {
            Build.DEVICE?.lowercase() ?: "unknown"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device code name", e)
            "unknown"
        }
    }

    suspend fun getAndroidSdkVersion(): Int = withContext(Dispatchers.IO) {
        Build.VERSION.SDK_INT
    }

    suspend fun getNetworkOperator(): String = withContext(Dispatchers.IO) {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.networkOperator ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network operator", e)
            ""
        }
    }

    @Suppress("DEPRECATION")
    suspend fun getIMSI(): String = withContext(Dispatchers.IO) {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.subscriberId ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IMSI", e)
            ""
        }
    }
}
