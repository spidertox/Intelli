package com.krcast.sender.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.krcast.sender.domain.model.DiscoveredDevice
import com.krcast.sender.domain.model.WifiP2pState
import com.krcast.sender.ui.components.*
import com.krcast.sender.ui.theme.*
import com.krcast.sender.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DiscoveryScreen(
    onBack: () -> Unit,
    onConnect: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.wifiP2pState.collectAsState()
    val devices = if (state is WifiP2pState.Scanning) state.devices else emptyList()

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
                text = "Device Discovery",
                style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFFFFF))
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Scan for nearby Wi‑Fi Direct peers",
            style = MaterialTheme.typography.bodyMedium.copy(color = textMuted),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalArrangement(androidx.compose.foundation.layout.Arrangement.SpaceBetween)
                .verticalAlignment(Alignment.CenterVertically)
        ) {
            Text(
                text = when (state) {
                    is WifiP2pState.Scanning -> "Scanning…"
                    is WifiP2pState.Failed -> "Scan failed"
                    else -> "Tap scan to discover devices"
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = textMuted)
            )
            CastButton(
                onClick = { viewModel.startScanning() },
                enabled = state !is WifiP2pState.Scanning,
                label = "Scan",
                modifier = Modifier.width(100.dp).height(40.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Discovered Devices",
            style = MaterialTheme.typography.labelLarge.copy(color = textMuted),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
        ) {
            items(devices) { device ->
                DeviceItem(
                    device = device,
                    onClick = { viewModel.connect(device) }
                )
            }
        }

        if (devices.isEmpty() && state !is WifiP2pState.Scanning) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalArrangement(androidx.compose.foundation.layout.Arrangement.Center)
                    .horizontalAlignment(Alignment.CenterHorizontally)
                    .weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "No devices",
                        tint = textMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No devices found",
                        style = MaterialTheme.typography.bodyMedium.copy(color = textMuted)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        CastButton(
            onClick = onConnect,
            label = "Connect to TV Guide",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(48.dp)
        )
    }
}

@Composable
private fun DeviceItem(device: DiscoveredDevice, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF16161F), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(Color(0xFF1F6FFF), CircleShape)
                .clip(CircleShape)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = "Device",
                tint = Color(0xFFFFFFFF),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(
            Modifier.weight(1f),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = device.deviceName,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFFFFFFF))
            )
            Text(
                text = "Compatibility: ${device.compatibilityStatus()}",
                style = MaterialTheme.typography.bodySmall.copy(color = statusUnknown)
            )
        }
        if (device.connectionState != 0) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Connected",
                tint = statusConnected
            )
        }
    }
}
