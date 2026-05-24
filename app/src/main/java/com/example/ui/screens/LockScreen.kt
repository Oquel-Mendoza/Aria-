package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalPremiumTheme
import com.example.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val userProfile by viewModel.userProfile.collectAsState()
    val enteredPin by viewModel.enteredPin.collectAsState()
    val pinError by viewModel.pinError.collectAsState()

    val username = userProfile?.username ?: "Usuario"

    // Error shake animation offset
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(pinError) {
        if (pinError) {
            // Shake back and forth
            repeat(4) {
                shakeOffset.animateTo(12f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                shakeOffset.animateTo(-12f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
            }
            shakeOffset.animateTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing aura behind
        Box(
            modifier = Modifier
                .size(250.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(theme.primary.copy(alpha = 0.07f), Color.Transparent)
                    )
                )
                .align(Alignment.TopCenter)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Profile Initials Bubble
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(theme.primary.copy(alpha = 0.15f))
                    .border(1.5.dp, theme.primary.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary
                )
            }

            // Greet
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hola, $username",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = theme.onBackground
                )
                Text(
                    text = "Introduce tu clave PIN de seguridad",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.secondary,
                    textAlign = TextAlign.Center
                )
            }

            // PIN Indicators offset by shake
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .offset(x = shakeOffset.value.dp)
                    .padding(vertical = 12.dp)
            ) {
                repeat(4) { idx ->
                    val isFilled = enteredPin.length > idx
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) theme.primary else theme.secondary.copy(alpha = 0.25f)
                            )
                            .border(
                                1.dp,
                                if (pinError) Color.Red else theme.primary.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }

            if (pinError) {
                Text(
                    text = "PIN Incorrecto. Ingrese su clave de nuevo.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(280.dp)
            ) {
                val digits = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "CLEAR")
                )

                for (row in digits) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (key in row) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .testTag(if (key.isNotEmpty()) "keypad_$key" else "keypad_empty"),
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "" -> {
                                        // Empty space for layout balance
                                    }
                                    "CLEAR" -> {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Borrar",
                                            tint = theme.secondary,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable {
                                                    if (enteredPin.isNotEmpty()) {
                                                        viewModel.enteredPin.value = enteredPin.dropLast(1)
                                                    }
                                                }
                                        )
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(theme.surface.copy(alpha = 0.5f))
                                                .border(
                                                    0.5.dp,
                                                    theme.secondary.copy(alpha = 0.15f),
                                                    CircleShape
                                                )
                                                .clickable {
                                                    if (enteredPin.length < 4) {
                                                        val newVal = enteredPin + key
                                                        viewModel.enteredPin.value = newVal
                                                        if (newVal.length == 4) {
                                                            // Auto authenticate
                                                            viewModel.loginWithPin(newVal)
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.onBackground
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
