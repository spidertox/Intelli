package com.krcast.sender.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.krcast.sender.domain.model.WifiP2pState
import com.krcast.sender.ui.components.*
import com.krcast.sender.ui.theme.*
import com.krcast.sender.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onFindTv: () -> Unit,
    onCompatibility: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val capabilities by viewModel.capabilities.collectAsState()
    val wifiP2pState by viewModel.wifiP2pState.collectAsState()

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(96.dp)
                .background(
                    Brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.9f).toArgb(),
                            Color(0xFF8E3DFF).copy(alpha = 0.9f).toArgb()
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clipToBounds()
        ) {
            Icon(
                imageVector = Icons.Default.Cast,
                contentDescription = "KR Cast",
                tint = Color(0xFFFFFFFF),
                modifier = Modifier.align(Alignment.Center).size(40.dp)
            )
        }
        Spacer(Modifier.height(32.dp))

        Text(
            text = "KR Cast",
            style = MaterialTheme.typography.headlineLarge.copy(color = Color(0xFFFFFFFF))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your Screen. Your Big Display.",
            style = MaterialTheme.typography.bodyLarge.copy(color = textMuted),
            textAlign = androidx.compose.ui.text.TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(180.dp)
        ) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text("Wi‑Fi Direct", style = MaterialTheme.typography.labelLarge.copy(color = textMuted))
                    when (wifiP2pState) {
                        is WifiP2pState.Enabled -> StatusBadge("Available", statusConnected, Modifier)
                        is WifiP2pState.Disabled -> StatusBadge("Not available", statusDisconnected, Modifier)
                        is WifiP2pState.Scanning -> StatusBadge("Scanning…", statusConnecting, Modifier)
                        is WifiP2pState.Connecting -> StatusBadge("Connecting…", statusConnecting, Modifier)
                        is WifiP2pState.Connected -> StatusBadge("Connected", statusConnected, Modifier)
                        is WifiP2pState.Failed -> StatusBadge("Failed", statusDisconnected, Modifier)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text("Miracast Support", style = MaterialTheme.typography.labelLarge.copy(color = textMuted))
                    val native = capabilities?.nativeMiracastAvailable ?: false
                    StatusBadge(
                        if (native) "Available" else "Not exposed",
                        if (native) statusConnected else statusWarning,
                        Modifier
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text("Status", style = MaterialTheme.typography.labelLarge.copy(color = textMuted))
                    StatusBadge(
                        if (capabilities?.nativeMiracastAvailable == true) "Ready" else "Limited",
                        if (capabilities?.nativeMiracastAvailable == true) statusConnected else statusWarning,
                        Modifier
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        CastButton(
            onClick = onFindTv,
            label = "Find My TV",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp)
        )

        Spacer(Modifier.height(16.dp))

        CastButton(
            onClick = onCompatibility,
            label = "Device Compatibility",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(48.dp)
        )
    }
}
