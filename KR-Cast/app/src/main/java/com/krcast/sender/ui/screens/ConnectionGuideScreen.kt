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
fun ConnectionGuideScreen(
    onBack: () -> Unit,
    onCast: () -> Unit
) {
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
                text = "Connect to Samsung TV",
                style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFFFFF))
            )
        }

        Spacer(Modifier.height(16.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Setup Guide",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFFFFF))
                )
                Spacer(Modifier.height(16.dp))

                val steps = listOf(
                    "Turn on your Samsung TV.",
                    "Open Screen Mirroring or Device Connect Manager on the TV.",
                    "Enable receiving mode on the TV.",
                    "Ensure phone and TV can establish a direct wireless connection.",
                    "Follow the phone's supported system-level connection flow, if available."
                )

                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .background(Color(0xFF1F6FFF), CircleShape)
                                .clip(CircleShape)
                                .alignment(Alignment.Top)
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFFFFFFFF)),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFE8E8F0)),
                            modifier = Modifier.weight(1f).paddingTop(2.dp)
                        )
                    }
                    if (index < steps.lastIndex) Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Wi‑Fi Direct discovery and actual screen mirroring are different operations. " +
                    "Discovery finding a device does not guarantee Miracast streaming will work.",
                    style = MaterialTheme.typography.bodySmall.copy(color = textMuted),
                    lineHeight = 20.dp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        CastButton(
            onClick = onCast,
            label = "Open Casting Dashboard",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp)
        )
    }
}
