package com.krcast.sender.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.krcast.sender.domain.model.MirroringCapability
import com.krcast.sender.ui.components.*
import com.krcast.sender.ui.theme.*
import com.krcast.sender.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val capabilities by viewModel.capabilities.collectAsState(value = MirroringCapability())

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
                .background(Color(0xFF0A0A0F))
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFFFFFFF)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Diagnostics",
                style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFFFFF))
            )
        }

        Spacer(Modifier.height(16.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Device Information",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFFFFF))
                )
                Spacer(Modifier.height(16.dp))

                DiagnosticRow("Android Version", android.os.Build.VERSION.RELEASE)
                DiagnosticRow("Device Model", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                DiagnosticRow("Wi‑Fi Direct", if (capabilities.wifiDirectAvailable) "Available" else "Not available")
                DiagnosticRow("MediaProjection", if (capabilities.mediaProjectionAvailable) "Available" else "Not available")
                DiagnosticRow("Hardware Encoder", if (capabilities.hardwareEncoderAvailable) "Available" else "Not available")
                DiagnosticRow("Miracast (Native)", if (capabilities.nativeMiracastAvailable) "Available" else "Not exposed")

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Limitations",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFFFFF))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = capabilities.limitationMessage ?: "No limitations detected.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = statusWarning)
                )

                Spacer(Modifier.height(20.dp))

                CastButton(
                    onClick = {},
                    label = "Copy Diagnostic Report",
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = textMuted)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFFFFFFF))
        )
    }
}
