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
import com.krcast.sender.ui.components.*
import com.krcast.sender.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit) {
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
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFFFFF))
            )
        }

        Spacer(Modifier.height(16.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "KR Cast",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFFFFF))
                )
                Text(
                    text = "Version 0.1.0",
                    style = MaterialTheme.typography.bodyMedium.copy(color = textMuted)
                )
                Spacer(Modifier.height(16.dp))
                SettingsRow(
                    icon = Icons.Default.Info,
                    label = "About KR Cast",
                    onClick = {}
                )
                Divider(color = Color(0xFF2A2A3A))
                SettingsRow(
                    icon = Icons.Default.DarkMode,
                    label = "Theme (Dark/Light)",
                    onClick = {}
                )
                Divider(color = Color(0xFF2A2A3A))
                SettingsRow(
                    icon = Icons.Default.Help,
                    label = "Troubleshooting Guide",
                    onClick = {}
                )
                Divider(color = Color(0xFF2A2A3A))
                SettingsRow(
                    icon = Icons.Default.Send,
                    label = "Privacy Policy",
                    onClick = {}
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .horizontalAlignment(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "KR Cast does not collect personal data or upload screen content.",
                style = MaterialTheme.typography.bodySmall.copy(color = textMuted),
                textAlign = androidx.compose.ui.text.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF1F6FFF),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFFFFFFF))
        )
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Next",
            tint = textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
