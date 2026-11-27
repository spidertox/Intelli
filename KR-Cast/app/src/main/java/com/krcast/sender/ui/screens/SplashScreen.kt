package com.krcast.sender.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.krcast.sender.ui.components.*
import com.krcast.sender.ui.theme.*
import com.krcast.sender.theme.KRCastTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val opacity by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900)
    )

    LaunchedEffect(Unit) {
        delay(1800)
        onFinished()
    }

    Box(
        Modifier.fillMaxSize().background(Color(0xFF0A0A0F)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = opacity > 0.5f, enter = fadeIn(), exit = fadeOut()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(80.dp)
                        .background(
                            Brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    primaryColor.toArgb(),
                                    Color(0xFF8E3DFF).toArgb()
                                )
                            ),
                            shape = CircleShape
                        )
                        .clipToBounds()
                ) {
                    Icon(
                        imageVector = Icons.Default.Cast,
                        contentDescription = "KR Cast",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "KR Cast",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color(0xFFFFFFFF)
                    )
                )
                Text(
                    text = "Your Screen. Your Big Display.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textMuted
                    )
                )
            }
        }
    }
}
