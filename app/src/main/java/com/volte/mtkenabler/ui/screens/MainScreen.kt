package com.volte.mtkenabler.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.volte.mtkenabler.model.VoLTEConfig
import com.volte.mtkenabler.model.VoLTEStatus
import com.volte.mtkenabler.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val deviceProfile by viewModel.deviceProfile.collectAsState()
    val voLTEStatus by viewModel.voLTEStatus.collectAsState()
    val voWiFiStatus by viewModel.voWiFiStatus.collectAsState()
    val isRooted by viewModel.isRooted.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationResult by viewModel.operationResult.collectAsState()
    val logMessages by viewModel.logMessages.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MTK VoLTE Enabler") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.checkRootAccess() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "معلومات الجهاز",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    deviceInfo?.let { info ->
                        InfoRow("الشركة المصنعة", info.manufacturer)
                        InfoRow("الموديل", info.model)
                        InfoRow("الرقاقة", info.chipset)
                        InfoRow("إصدار Android", "${info.androidVersion} (SDK ${info.sdkVersion})")
                        InfoRow(
                            "حالة Root",
                            if (info.isRooted) "✓ متاح" else "✗ غير متاح",
                            if (info.isRooted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                        InfoRow(
                            "دعم MTK",
                            if (info.isMTK) "✓ مدعوم" else "✗ غير مدعوم",
                            if (info.isMTK) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    } ?: run {
                        Text(
                            text = "جاري التحميل...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الحالة الحالية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    StatusRow("VoLTE", voLTEStatus)
                    StatusRow("VoWiFi", voWiFiStatus)
                }
            }

            // Action Buttons
            if (isRooted) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تفعيل VoLTE")
                    }

                    Button(
                        onClick = { viewModel.enableVoWiFi() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تفعيل VoWiFi")
                    }

                    OutlinedButton(
                        onClick = { viewModel.disableVoLTE() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إلغاء التفعيل")
                    }

                    OutlinedButton(
                        onClick = { viewModel.createBackup() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنشاء نسخة احتياطية")
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "صلاحيات Root مطلوبة",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "يرجى تثبيت Magisk أو KernelSU ومنح صلاحيات Root للتطبيق",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Operation Result
            operationResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (result.success) Icons.Default.CheckCircle
                                else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.success)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = result.message,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (result.details.isNotEmpty()) {
                            Text(
                                text = result.details,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Logs
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل العمليات",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.clearLogs() }) {
                            Icon(Icons.Default.Delete, contentDescription = "مسح السجل")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(logMessages) { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showConfigDialog) {
            VoLTEConfigDialog(
                onDismiss = { showConfigDialog = false },
                onConfirm = { config ->
                    viewModel.enableVoLTE(config)
                    showConfigDialog = false
                }
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun StatusRow(label: String, status: VoLTEStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                when (status) {
                    VoLTEStatus.ENABLED -> Icons.Default.CheckCircle
                    VoLTEStatus.DISABLED -> Icons.Default.Cancel
                    VoLTEStatus.UNKNOWN -> Icons.Default.Help
                    VoLTEStatus.ERROR -> Icons.Default.Error
                },
                contentDescription = null,
                tint = when (status) {
                    VoLTEStatus.ENABLED -> MaterialTheme.colorScheme.primary
                    VoLTEStatus.DISABLED -> MaterialTheme.colorScheme.error
                    VoLTEStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                    VoLTEStatus.ERROR -> MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = when (status) {
                    VoLTEStatus.ENABLED -> "مفعّل"
                    VoLTEStatus.DISABLED -> "معطّل"
                    VoLTEStatus.UNKNOWN -> "غير معروف"
                    VoLTEStatus.ERROR -> "خطأ"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoLTEConfigDialog(
    onDismiss: () -> Unit,
    onConfirm: (VoLTEConfig) -> Unit
) {
    var carrierName by remember { mutableStateOf("") }
    var mcc by remember { mutableStateOf("") }
    var mnc by remember { mutableStateOf("") }
    var enableVoWiFi by remember { mutableStateOf(true) }
    var enableVideoCalling by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إعدادات VoLTE")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = carrierName,
                    onValueChange = { carrierName = it },
                    label = { Text("اسم شركة الاتصالات (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mcc,
                    onValueChange = { mcc = it },
                    label = { Text("MCC (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mnc,
                    onValueChange = { mnc = it },
                    label = { Text("MNC (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تفعيل VoWiFi")
                    Switch(
                        checked = enableVoWiFi,
                        onCheckedChange = { enableVoWiFi = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تفعيل مكالمات الفيديو")
                    Switch(
                        checked = enableVideoCalling,
                        onCheckedChange = { enableVideoCalling = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        VoLTEConfig(
                            enableVoLTE = true,
                            enableVoWiFi = enableVoWiFi,
                            enableVideoCalling = enableVideoCalling,
                            carrierName = carrierName,
                            mcc = mcc,
                            mnc = mnc
                        )
                    )
                }
            ) {
                Text("تفعيل")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
