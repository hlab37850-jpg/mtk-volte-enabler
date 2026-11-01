package com.volte.mtkenabler.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.volte.mtkenabler.model.DeviceInfo
import com.volte.mtkenabler.model.DeviceProfile
import com.volte.mtkenabler.model.OperationResult
import com.volte.mtkenabler.model.VoLTEConfig
import com.volte.mtkenabler.model.VoLTEStatus
import com.volte.mtkenabler.repository.DeviceRepository
import com.volte.mtkenabler.repository.RootRepository
import com.volte.mtkenabler.repository.VoLTERepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"

    private val rootRepository = RootRepository()
    private val deviceRepository = DeviceRepository(application)
    private val volteRepository = VoLTERepository(rootRepository)

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()

    private val _deviceProfile = MutableStateFlow<DeviceProfile?>(null)
    val deviceProfile: StateFlow<DeviceProfile?> = _deviceProfile.asStateFlow()

    private val _voLTEStatus = MutableStateFlow(VoLTEStatus.UNKNOWN)
    val voLTEStatus: StateFlow<VoLTEStatus> = _voLTEStatus.asStateFlow()

    private val _voWiFiStatus = MutableStateFlow(VoLTEStatus.UNKNOWN)
    val voWiFiStatus: StateFlow<VoLTEStatus> = _voWiFiStatus.asStateFlow()

    private val _isRooted = MutableStateFlow(false)
    val isRooted: StateFlow<Boolean> = _isRooted.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationResult = MutableStateFlow<OperationResult?>(null)
    val operationResult: StateFlow<OperationResult?> = _operationResult.asStateFlow()

    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            try {
                addLog("جاري تهيئة التطبيق...")
                checkRootAccess()
                loadDeviceInfo()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing app", e)
                addLog("خطأ في تهيئة التطبيق: ${e.message}")
            }
        }
    }

    fun loadDeviceInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            addLog("جاري تحميل معلومات الجهاز...")

            try {
                val info = deviceRepository.getDeviceInfo()
                _deviceInfo.value = info.copy(isRooted = _isRooted.value)

                val codename = deviceRepository.getDeviceCodeName()
                val profile = DeviceProfile.getProfileByCodename(codename)
                _deviceProfile.value = profile

                addLog("الجهاز: ${info.manufacturer} ${info.model}")
                addLog("الرقاقة: ${info.chipset}")
                addLog("إصدار Android: ${info.androidVersion} (SDK ${info.sdkVersion})")
                addLog("كود الجهاز: $codename")

                if (profile != null) {
                    addLog("تم التعرف على الجهاز: ${profile.deviceName}")
                    addLog("الرقاقة المدعومة: ${profile.chipset}")
                } else {
                    addLog("تحذير: الجهاز غير مدعوم رسميًا، قد لا تعمل بعض الميزات")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading device info", e)
                addLog("خطأ في تحميل معلومات الجهاز: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkRootAccess() {
        viewModelScope.launch {
            addLog("جاري التحقق من صلاحيات Root...")
            try {
                val rooted = rootRepository.checkRootAccess()
                _isRooted.value = rooted
                _deviceInfo.value = _deviceInfo.value?.copy(isRooted = rooted)

                if (rooted) {
                    addLog("✓ صلاحيات Root متاحة")
                    checkVoLTEStatus()
                    checkVoWiFiStatus()
                } else {
                    addLog("✗ صلاحيات Root غير متاحة")
                    addLog("التطبيق يحتاج إلى صلاحيات Root للعمل")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking root access", e)
                addLog("خطأ في التحقق من Root: ${e.message}")
                _isRooted.value = false
            }
        }
    }

    fun checkVoLTEStatus() {
        viewModelScope.launch {
            if (!_isRooted.value) {
                addLog("لا يمكن التحقق من الحالة بدون صلاحيات Root")
                return@launch
            }

            addLog("جاري التحقق من حالة VoLTE...")
            try {
                _voLTEStatus.value = volteRepository.checkVoLTEStatus()
                addLog("حالة VoLTE: ${_voLTEStatus.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking VoLTE status", e)
                addLog("خطأ في التحقق من VoLTE: ${e.message}")
                _voLTEStatus.value = VoLTEStatus.ERROR
            }
        }
    }

    fun checkVoWiFiStatus() {
        viewModelScope.launch {
            if (!_isRooted.value) {
                addLog("لا يمكن التحقق من الحالة بدون صلاحيات Root")
                return@launch
            }

            addLog("جاري التحقق من حالة VoWiFi...")
            try {
                _voWiFiStatus.value = volteRepository.checkVoWiFiStatus()
                addLog("حالة VoWiFi: ${_voWiFiStatus.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking VoWiFi status", e)
                addLog("خطأ في التحقق من VoWiFi: ${e.message}")
                _voWiFiStatus.value = VoLTEStatus.ERROR
            }
        }
    }

    fun enableVoLTE(config: VoLTEConfig) {
        viewModelScope.launch {
            if (!_isRooted.value) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "صلاحيات Root غير متاحة"
                )
                return@launch
            }

            val profile = _deviceProfile.value
            if (profile == null) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "ملف تعريف الجهاز غير متوفر"
                )
                return@launch
            }

            _isLoading.value = true
            addLog("جاري تفعيل VoLTE...")

            try {
                val result = volteRepository.enableVoLTE(profile, config)
                _operationResult.value = result

                if (result.success) {
                    addLog("✓ ${result.message}")
                    checkVoLTEStatus()
                } else {
                    addLog("✗ ${result.message}")
                    if (result.details.isNotEmpty()) {
                        addLog("التفاصيل: ${result.details}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error enabling VoLTE", e)
                _operationResult.value = OperationResult(
                    success = false,
                    message = "خطأ في تفعيل VoLTE",
                    details = e.message ?: "Unknown error"
                )
                addLog("خطأ: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun enableVoWiFi() {
        viewModelScope.launch {
            if (!_isRooted.value) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "صلاحيات Root غير متاحة"
                )
                return@launch
            }

            val profile = _deviceProfile.value
            if (profile == null) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "ملف تعريف الجهاز غير متوفر"
                )
                return@launch
            }

            _isLoading.value = true
            addLog("جاري تفعيل VoWiFi...")

            try {
                val result = volteRepository.enableVoWiFi(profile)
                _operationResult.value = result

                if (result.success) {
                    addLog("✓ ${result.message}")
                    checkVoWiFiStatus()
                } else {
                    addLog("✗ ${result.message}")
                    if (result.details.isNotEmpty()) {
                        addLog("التفاصيل: ${result.details}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error enabling VoWiFi", e)
                _operationResult.value = OperationResult(
                    success = false,
                    message = "خطأ في تفعيل VoWiFi",
                    details = e.message ?: "Unknown error"
                )
                addLog("خطأ: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun disableVoLTE() {
        viewModelScope.launch {
            if (!_isRooted.value) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "صلاحيات Root غير متاحة"
                )
                return@launch
            }

            _isLoading.value = true
            addLog("جاري إلغاء تفعيل VoLTE...")

            try {
                val result = volteRepository.disableVoLTE()
                _operationResult.value = result

                if (result.success) {
                    addLog("✓ ${result.message}")
                    checkVoLTEStatus()
                } else {
                    addLog("✗ ${result.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error disabling VoLTE", e)
                _operationResult.value = OperationResult(
                    success = false,
                    message = "خطأ في إلغاء تفعيل VoLTE",
                    details = e.message ?: "Unknown error"
                )
                addLog("خطأ: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            if (!_isRooted.value) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "صلاحيات Root غير متاحة"
                )
                return@launch
            }

            val profile = _deviceProfile.value
            if (profile == null) {
                _operationResult.value = OperationResult(
                    success = false,
                    message = "ملف تعريف الجهاز غير متوفر"
                )
                return@launch
            }

            _isLoading.value = true
            addLog("جاري إنشاء نسخة احتياطية...")

            try {
                val backupDir = "/sdcard/MTKVoLTE_Backup"
                val mkdirResult = rootRepository.executeSuCommand("mkdir -p $backupDir")

                if (mkdirResult.isSuccess) {
                    val result = volteRepository.backupBuildProp(profile, backupDir)
                    _operationResult.value = result

                    if (result.success) {
                        addLog("✓ ${result.message}")
                        addLog("موقع النسخة الاحتياطية: $backupDir")
                    } else {
                        addLog("✗ ${result.message}")
                    }
                } else {
                    addLog("✗ فشل إنشاء مجلد النسخ الاحتياطي")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating backup", e)
                _operationResult.value = OperationResult(
                    success = false,
                    message = "خطأ في إنشاء النسخة الاحتياطية",
                    details = e.message ?: "Unknown error"
                )
                addLog("خطأ: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun addLog(message: String) {
        try {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            _logMessages.value = _logMessages.value + "[$timestamp] $message"
        } catch (e: Exception) {
            Log.e(TAG, "Error adding log", e)
        }
    }

    fun clearLogs() {
        _logMessages.value = emptyList()
    }
}
