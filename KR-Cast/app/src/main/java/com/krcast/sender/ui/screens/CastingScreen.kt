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
fun CastingScreen(onStop: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
                .background(Color(0xFF0A0A0F))
        ) {
            IconButton(onClick = onStop) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop casting",
                    tint = Color(0xFFFFFFFF)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Casting Dashboard",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFFFFF))
                )
                Text(
                    text = "Samsung TV",
                    style = MaterialTheme.typography.bodyMedium.copy(color = textMuted)
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFFFFFFFF)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalArrangement(androidx.compose.foundation.layout.Arrangement.Center)
                .verticalAlignment(Alignment.CenterVertically)
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .background(statusConnected, CircleShape)
                    .clip(CircleShape)
            )
            Text(
                text = "Screen sharing active",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFFFFFFF),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            )
        }

        Spacer(Modifier.height(32.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(140.dp)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text("TV Device", style = MaterialTheme.typography.labelLarge.copy(color = textMuted))
                    Text("Samsung TV", style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFFFFFFF)))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text("Status", style = MaterialTheme.typography.labelLarge.copy(color = textMuted))
                    StatusBadge("Connected", statusConnected, Modifier)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text("Resolution", style = MaterialTheme.typography.labelLarge.copy(color = textMuted))
                    Text("1920×1080 (target)", style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFFFFFFF)))
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        CastButton(
            onClick = onStop,
            label = "Stop Casting",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp)
        )
    }
}
