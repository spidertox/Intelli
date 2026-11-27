package com.krcast.sender.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.krcast.sender.ui.theme.primaryColor
import com.krcast.sender.ui.theme.onPrimaryColor

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clipToBounds()
            .background(
                Color(0xFF16161F).copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color(0xFF2A2A3A).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    ) {
        content()
    }
}

@Composable
fun CastButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    label: String = "Cast",
    modifier: Modifier = Modifier
) {
    val colors = ButtonDefaults.buttonColors(
        containerColor = if (enabled) Color(0xFF1F6FFF) else Color(0xFF2A2A3A),
        contentColor = Color(0xFFFFFFFF)
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth().height(56.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 2.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun StatusBadge(
    status: String,
    color: Color = Color(0xFF1F6FFF),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
fun IconButtonCircle(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Box(
            Modifier.size(48.dp, 48.dp)
                .background(Color(0xFF16161F), CircleShape)
                .clip(CircleShape)
        ) {
            icon()
        }
    }
}
