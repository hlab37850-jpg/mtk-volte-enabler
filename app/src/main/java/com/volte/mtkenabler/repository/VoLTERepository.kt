package com.volte.mtkenabler.repository

import android.util.Log
import com.volte.mtkenabler.model.DeviceProfile
import com.volte.mtkenabler.model.OperationResult
import com.volte.mtkenabler.model.VoLTEConfig
import com.volte.mtkenabler.model.VoLTEStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VoLTERepository(private val rootRepository: RootRepository) {
    private val TAG = "VoLTERepository"

    suspend fun checkVoLTEStatus(): VoLTEStatus = withContext(Dispatchers.IO) {
        try {
            val result = rootRepository.executeSuCommand(
                "getprop persist.dbg.volte_avail_ovr"
            )

            if (result.isSuccess && result.out.isNotEmpty()) {
                val value = result.out.first().trim()
                when (value) {
                    "1" -> VoLTEStatus.ENABLED
                    "0" -> VoLTEStatus.DISABLED
                    else -> VoLTEStatus.UNKNOWN
                }
            } else {
                VoLTEStatus.UNKNOWN
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check VoLTE status", e)
            VoLTEStatus.ERROR
        }
    }

    suspend fun checkVoWiFiStatus(): VoLTEStatus = withContext(Dispatchers.IO) {
        try {
            val result = rootRepository.executeSuCommand(
                "getprop persist.dbg.vt_avail_ovr"
            )

            if (result.isSuccess && result.out.isNotEmpty()) {
                val value = result.out.first().trim()
                when (value) {
                    "1" -> VoLTEStatus.ENABLED
                    "0" -> VoLTEStatus.DISABLED
                    else -> VoLTEStatus.UNKNOWN
                }
            } else {
                VoLTEStatus.UNKNOWN
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check VoWiFi status", e)
            VoLTEStatus.ERROR
        }
    }

    suspend fun enableVoLTE(profile: DeviceProfile, config: VoLTEConfig): OperationResult = withContext(Dispatchers.IO) {
        try {
            val commands = mutableListOf<String>()

            // Remount system
            commands.add("mount -o rw,remount /system")
            commands.add("mount -o rw,remount /vendor")

            // Set VoLTE properties
            commands.add("setprop persist.dbg.volte_avail_ovr 1")
            commands.add("setprop persist.sys.ss.volte.support true")
            commands.add("setprop persist.radio.volte.enable true")
            commands.add("setprop persist.sys.ims.support true")
            commands.add("setprop persist.ims.voice.enable 1")

            // MTK specific properties
            commands.add("setprop persist.radio.mtk_ps2_rat lte")
            commands.add("setprop persist.radio.mtk_ps3_rat lte")
            commands.add("setprop persist.sys.modem.lte.data.mode 1")
            commands.add("setprop persist.sys.mtk.volte.enable 1")

            // Carrier config
            if (config.carrierName.isNotEmpty()) {
                commands.add("setprop persist.sys.carrier.name ${config.carrierName}")
            }

            if (config.mcc.isNotEmpty() && config.mnc.isNotEmpty()) {
                commands.add("setprop persist.sys.mcc ${config.mcc}")
                commands.add("setprop persist.sys.mnc ${config.mnc}")
            }

            val result = rootRepository.executeSuCommand(commands.joinToString(" && "))

            OperationResult(
                success = result.isSuccess,
                message = if (result.isSuccess) "تم تفعيل VoLTE بنجاح" else "فشل تفعيل VoLTE",
                details = result.out.joinToString("\n")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable VoLTE", e)
            OperationResult(
                success = false,
                message = "خطأ في تفعيل VoLTE",
                details = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun enableVoWiFi(profile: DeviceProfile): OperationResult = withContext(Dispatchers.IO) {
        try {
            val commands = listOf(
                "setprop persist.dbg.vt_avail_ovr 1",
                "setprop persist.sys.ims.vowifi.enable 1",
                "setprop persist.radio.vowifi.enable true",
                "setprop persist.sys.vowifi.support true"
            )

            val result = rootRepository.executeSuCommand(commands.joinToString(" && "))

            OperationResult(
                success = result.isSuccess,
                message = if (result.isSuccess) "تم تفعيل VoWiFi بنجاح" else "فشل تفعيل VoWiFi",
                details = result.out.joinToString("\n")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable VoWiFi", e)
            OperationResult(
                success = false,
                message = "خطأ في تفعيل VoWiFi",
                details = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun disableVoLTE(): OperationResult = withContext(Dispatchers.IO) {
        try {
            val commands = listOf(
                "setprop persist.dbg.volte_avail_ovr 0",
                "setprop persist.sys.ss.volte.support false",
                "setprop persist.radio.volte.enable false",
                "setprop persist.sys.ims.support false"
            )

            val result = rootRepository.executeSuCommand(commands.joinToString(" && "))

            OperationResult(
                success = result.isSuccess,
                message = if (result.isSuccess) "تم إلغاء تفعيل VoLTE" else "فشل إلغاء تفعيل VoLTE",
                details = result.out.joinToString("\n")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable VoLTE", e)
            OperationResult(
                success = false,
                message = "خطأ في إلغاء تفعيل VoLTE",
                details = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun backupBuildProp(profile: DeviceProfile, backupDir: String): OperationResult = withContext(Dispatchers.IO) {
        try {
            val sourcePath = profile.buildPropPath
            val backupPath = "$backupDir/build.prop.backup"

            val result = rootRepository.executeSuCommand("cp -p $sourcePath $backupPath")

            OperationResult(
                success = result.isSuccess,
                message = if (result.isSuccess) "تم إنشاء نسخة احتياطية من build.prop" else "فشل إنشاء النسخة الاحتياطية",
                details = result.out.joinToString("\n")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to backup build.prop", e)
            OperationResult(
                success = false,
                message = "خطأ في إنشاء النسخة الاحتياطية",
                details = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun modifyBuildProp(profile: DeviceProfile, properties: Map<String, String>): OperationResult = withContext(Dispatchers.IO) {
        try {
            val commands = mutableListOf<String>()

            // Remount system
            commands.add("mount -o rw,remount /system")

            // Apply properties
            properties.forEach { (key, value) ->
                commands.add("sed -i 's/^$key=.*/$key=$value/' ${profile.buildPropPath}")
                commands.add("echo '$key=$value' >> ${profile.buildPropPath}")
            }

            val result = rootRepository.executeSuCommand(commands.joinToString(" && "))

            OperationResult(
                success = result.isSuccess,
                message = if (result.isSuccess) "تم تعديل build.prop بنجاح" else "فشل تعديل build.prop",
                details = result.out.joinToString("\n")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to modify build.prop", e)
            OperationResult(
                success = false,
                message = "خطأ في تعديل build.prop",
                details = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun restoreBuildProp(backupPath: String, profile: DeviceProfile): OperationResult = withContext(Dispatchers.IO) {
        try {
            val commands = listOf(
                "mount -o rw,remount /system",
                "cp -p $backupPath ${profile.buildPropPath}"
            )

            val result = rootRepository.executeSuCommand(commands.joinToString(" && "))

            OperationResult(
                success = result.isSuccess,
                message = if (result.isSuccess) "تم استعادة build.prop بنجاح" else "فشل استعادة build.prop",
                details = result.out.joinToString("\n")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore build.prop", e)
            OperationResult(
                success = false,
                message = "خطأ في استعادة build.prop",
                details = e.message ?: "Unknown error"
            )
        }
    }
}
