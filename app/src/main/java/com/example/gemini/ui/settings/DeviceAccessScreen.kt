package com.example.gemini.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.menuAnchor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemini.data.adb.AdbConnectManager
import com.example.gemini.data.deviceaccess.DeviceAccessManager
import com.example.gemini.data.deviceaccess.DeviceAccessAuditLog
import com.example.gemini.data.deviceaccess.DeviceAccessProvider
import com.example.gemini.data.deviceaccess.DeviceCommandRequest
import com.example.gemini.data.deviceaccess.DeviceCommandResult
import com.example.gemini.data.deviceaccess.ProviderAvailability
import com.example.gemini.data.deviceaccess.ProviderStatus
import com.example.gemini.data.deviceaccess.RootedUbuntuManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceAccessScreen(cardBg: androidx.compose.ui.graphics.Color, cardBorder: androidx.compose.foundation.BorderStroke, onOpenAdbConnect: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var provider by remember { mutableStateOf(DeviceAccessProvider.NORMAL) }
    var statuses by remember { mutableStateOf(DeviceAccessProvider.entries.associateWith { ProviderStatus(it, ProviderAvailability.DISCONNECTED) }) }
    var auditEntries by remember { mutableStateOf(emptyList<org.json.JSONObject>()) }
    var command by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(emptyList<String>()) }
    var selectedDevice by remember { mutableStateOf<String?>(null) }
    var devices by remember { mutableStateOf(emptyList<AdbConnectManager.Device>()) }
    var result by remember { mutableStateOf<DeviceCommandResult?>(null) }
    var running by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingRoot by remember { mutableStateOf(false) }
    var pendingUbuntuInstall by remember { mutableStateOf(false) }
    var pendingUbuntuRemove by remember { mutableStateOf(false) }
    var ubuntuProgress by remember { mutableStateOf<Int?>(null) }
    var ubuntuProgressText by remember { mutableStateOf("") }
    var activeId by remember { mutableStateOf<String?>(null) }
    var executionJob by remember { mutableStateOf<Job?>(null) }
    var providerExpanded by remember { mutableStateOf(false) }
    var deviceExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        DeviceAccessManager.init(context)
        while (true) {
            val snapshot = linkedMapOf<DeviceAccessProvider, ProviderStatus>()
            DeviceAccessProvider.entries.forEach { snapshot[it] = DeviceAccessManager.status(it) }
            statuses = snapshot
            auditEntries = DeviceAccessAuditLog.entries()
            devices = AdbConnectManager.state.value.devices.filter { it.state == "device" }
            if (selectedDevice != null && devices.none { it.serial == selectedDevice }) selectedDevice = null
            delay(2_000)
        }
    }

    val currentStatus = statuses[provider] ?: ProviderStatus(provider, ProviderAvailability.ERROR)

    if (pendingRoot) {
        AlertDialog(
            onDismissRequest = { pendingRoot = false },
            icon = { Icon(Icons.Outlined.Security, contentDescription = null) },
            title = { Text(if (provider == DeviceAccessProvider.UBUNTU_CHROOT) "Confirm chroot command" else "Confirm root command") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Intelli will send this exact command through ${provider.label} using root authorization. Review it before continuing.")
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(command, Modifier.fillMaxWidth().padding(12.dp), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { pendingRoot = false; execute(true) }) { Text(if (provider == DeviceAccessProvider.UBUNTU_CHROOT) "Run in chroot" else "Run as root") }
            },
            dismissButton = { TextButton(onClick = { pendingRoot = false }) { Text("Cancel") } }
        )
    }

    if (pendingUbuntuInstall) AlertDialog(
        onDismissRequest = { pendingUbuntuInstall = false },
        title = { Text("Install verified Ubuntu root chroot?") },
        text = { Text("Download Ubuntu Base 24.04.5 for arm64, verify its pinned SHA-256, then extract to Intelli's private files directory using su. Root authorization will be requested by the root manager. This needs substantial free storage.") },
        confirmButton = { Button(onClick = {
            pendingUbuntuInstall = false
            ubuntuProgress = 0
            scope.launch {
                try {
                    RootedUbuntuManager.install(context, approved = true) { progress, message -> ubuntuProgress = progress; ubuntuProgressText = message }
                    DeviceAccessAuditLog.record("ubuntu-rootfs-install", DeviceAccessProvider.UBUNTU_CHROOT, 0, "completed")
                    error = null
                } catch (t: Throwable) {
                    DeviceAccessAuditLog.record("ubuntu-rootfs-install", DeviceAccessProvider.UBUNTU_CHROOT, null, "failed:${t.javaClass.simpleName}")
                    error = t.message ?: "Ubuntu installation failed."
                }
                finally { ubuntuProgress = null; statuses = DeviceAccessProvider.entries.associateWith { DeviceAccessManager.status(it) } }
            }
        }) { Text("Download and install") } },
        dismissButton = { TextButton(onClick = { pendingUbuntuInstall = false }) { Text("Cancel") } }
    )
    if (pendingUbuntuRemove) AlertDialog(
        onDismissRequest = { pendingUbuntuRemove = false },
        title = { Text("Remove Ubuntu root chroot?") },
        text = { Text("This permanently deletes Intelli's Ubuntu chroot rootfs. It does not delete Android system files. Root authorization is required.") },
        confirmButton = { Button(onClick = {
            pendingUbuntuRemove = false
            scope.launch {
                try {
                    RootedUbuntuManager.remove(context, approved = true)
                    DeviceAccessAuditLog.record("ubuntu-rootfs-remove", DeviceAccessProvider.UBUNTU_CHROOT, 0, "completed")
                    error = null
                } catch (t: Throwable) {
                    DeviceAccessAuditLog.record("ubuntu-rootfs-remove", DeviceAccessProvider.UBUNTU_CHROOT, null, "failed:${t.javaClass.simpleName}")
                    error = t.message ?: "Rootfs removal failed."
                }
                finally { statuses = DeviceAccessProvider.entries.associateWith { DeviceAccessManager.status(it) } }
            }
        }) { Text("Remove rootfs") } },
        dismissButton = { TextButton(onClick = { pendingUbuntuRemove = false }) { Text("Keep it") } }
    )

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Device Access", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text("Choose a provider for each command. Errors stay with that provider; Intelli will not retry through another one.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Card(colors = CardDefaults.cardColors(containerColor = cardBg), border = cardBorder) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Active provider", fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(expanded = providerExpanded, onExpandedChange = { providerExpanded = it }) {
                    OutlinedTextField(
                        value = provider.label,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(providerExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Provider") }
                    )
                    ExposedDropdownMenu(expanded = providerExpanded, onDismissRequest = { providerExpanded = false }) {
                        DeviceAccessProvider.entries.forEach { candidate ->
                            DropdownMenuItem(
                                text = { Text(candidate.label) },
                                onClick = { provider = candidate; result = null; error = null; providerExpanded = false }
                            )
                        }
                    }
                }
                StatusLine(currentStatus)
                if (provider == DeviceAccessProvider.SHIZUKU) {
                    Text("Shizuku must be installed and started separately. On Android 11+, users can start it with Wireless debugging; rooted users may start it with their root manager.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = DeviceAccessManager::requestShizukuPermission) { Text("Request Shizuku permission") }
                }
                if (provider == DeviceAccessProvider.ROOT) {
                    Text("Root is optional. A superuser prompt may appear after you confirm a command. Intelli does not store a root password.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (provider == DeviceAccessProvider.UBUNTU_CHROOT) {
                    Text("Real chroot, separate from Ubuntu PRoot. Requires arm64, a supported root manager, and mount-namespace support. The rootfs is Ubuntu Base 24.04.5 with a pinned SHA-256.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (ubuntuProgress != null) {
                        LinearProgressIndicator(progress = { (ubuntuProgress ?: 0) / 100f }, modifier = Modifier.fillMaxWidth())
                        Text(ubuntuProgressText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (RootedUbuntuManager.isInstalled(context)) {
                        Text("Disk use: ${RootedUbuntuManager.diskUsageBytes(context) / (1024 * 1024)} MB", fontSize = 12.sp)
                        OutlinedButton(onClick = { pendingUbuntuRemove = true }) { Text("Remove Ubuntu rootfs…") }
                    } else {
                        Button(onClick = { pendingUbuntuInstall = true }, enabled = RootedUbuntuManager.supportedArchitecture()) { Text("Install Ubuntu root chroot…") }
                    }
                }
                if (provider == DeviceAccessProvider.ADB || provider == DeviceAccessProvider.WIRELESS_ADB) {
                    TextButton(onClick = onOpenAdbConnect) { Text("Open ADB Connect settings") }
                    if (devices.size > 1) {
                        ExposedDropdownMenuBox(expanded = deviceExpanded, onExpandedChange = { deviceExpanded = it }) {
                            OutlinedTextField(
                                value = selectedDevice ?: "Select connected device",
                                onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(deviceExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(), label = { Text("Target device") }
                            )
                            ExposedDropdownMenu(expanded = deviceExpanded, onDismissRequest = { deviceExpanded = false }) {
                                devices.forEach { device -> DropdownMenuItem(text = { Text(device.serial) }, onClick = { selectedDevice = device.serial; deviceExpanded = false }) }
                            }
                        }
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = cardBg), border = cardBorder) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Command console", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { result = null; error = null }, enabled = result != null || error != null) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear output")
                    }
                    IconButton(onClick = { clipboard.setText(AnnotatedString(buildString { result?.let { append(it.stdout); if (it.stderr.isNotBlank()) append("\n\n[stderr]\n").append(it.stderr) } })) }, enabled = result != null) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy output")
                    }
                }
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text("Shell command") },
                    placeholder = { Text("Enter a command for ${provider.label}") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (!running) execute(false) })
                )
                if (history.isNotEmpty()) {
                    Text("Recent commands (this screen only)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    history.take(5).forEach { previous ->
                        TextButton(onClick = { command = previous }) {
                            Text(previous, fontFamily = FontFamily.Monospace, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(enabled = !running && command.isNotBlank() && (currentStatus.availability == ProviderAvailability.AVAILABLE || (provider == DeviceAccessProvider.ROOT && currentStatus.availability == ProviderAvailability.PERMISSION_REQUIRED)), onClick = {
                        if (provider == DeviceAccessProvider.ROOT || provider == DeviceAccessProvider.UBUNTU_CHROOT) pendingRoot = true else execute(false)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(" Run")
                    }
                    if (running) OutlinedButton(onClick = { activeId?.let(DeviceAccessManager::cancel); executionJob?.cancel(); running = false }) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp)); Text(" Cancel")
                    }
                    if (!running) TextButton(onClick = { scope.launch { statuses = DeviceAccessProvider.entries.associateWith { DeviceAccessManager.status(it) } } }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp)); Text(" Refresh")
                    }
                    if (running) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                result?.let { output ->
                    HorizontalDivider()
                    Text("${output.provider.label} · exit ${output.exitCode} · ${output.durationMs} ms${if (output.timedOut) " · timed out" else ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("stdout", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    Text(output.stdout.ifBlank { "(empty)" }, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    if (output.stderr.isNotBlank()) {
                        Text("stderr", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        Text(output.stderr, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Text("Provider status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        DeviceAccessProvider.entries.forEach { item ->
            val status = statuses[item] ?: ProviderStatus(item, ProviderAvailability.DISCONNECTED)
            Card(onClick = { provider = item; result = null; error = null }, colors = CardDefaults.cardColors(containerColor = cardBg), border = cardBorder) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        Text(status.availability.name.replace('_', ' '), color = statusColor(status.availability), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(status.detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (auditEntries.isNotEmpty()) {
            Text("Privileged operation audit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            auditEntries.take(20).forEach { entry ->
                Text(
                    "${entry.optString("provider")} · ${entry.optString("outcome")} · exit ${entry.opt("exitCode")} · ${entry.optString("commandSha256").take(12)}… · ${entry.optLong("time")}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    fun execute(approved: Boolean) {
        val id = UUID.randomUUID().toString()
        activeId = id
        history = (listOf(command) + history).distinct().take(20)
        running = true
        error = null
        executionJob = scope.launch {
            try {
                result = DeviceAccessManager.execute(
                    DeviceCommandRequest(provider, command, selectedDevice, timeoutSeconds = 60, privilegedApproved = approved, id = id)
                )
            } catch (t: Throwable) {
                if (t !is kotlinx.coroutines.CancellationException) error = t.message ?: "Command failed."
            } finally {
                running = false
                activeId = null
            }
        }
    }
}

@Composable
private fun StatusLine(status: ProviderStatus) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(50), color = statusColor(status.availability).copy(alpha = 0.16f)) {
            Text(status.availability.name.replace('_', ' '), Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = statusColor(status.availability), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(status.detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun statusColor(status: ProviderAvailability) = when (status) {
    ProviderAvailability.AVAILABLE -> MaterialTheme.colorScheme.primary
    ProviderAvailability.PERMISSION_REQUIRED -> MaterialTheme.colorScheme.tertiary
    ProviderAvailability.DISCONNECTED, ProviderAvailability.NOT_INSTALLED, ProviderAvailability.NOT_SUPPORTED -> MaterialTheme.colorScheme.onSurfaceVariant
    ProviderAvailability.ERROR -> MaterialTheme.colorScheme.error
}
