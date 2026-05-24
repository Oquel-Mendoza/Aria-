package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AriaTheme
import com.example.ui.theme.AriaThemes
import com.example.ui.theme.LocalPremiumTheme
import com.example.viewmodel.FinanceViewModel

@Composable
fun OnboardingScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }
    
    // Form fields
    var username by remember { mutableStateOf("") }
    var pinField by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var initialBalanceStr by remember { mutableStateOf("") }
    var selectedThemeId by remember { mutableStateOf("MINIMAL_WHITE") }

    var errorMsg by remember { mutableStateOf("") }

    val currentTheme = AriaThemes.getThemeById(selectedThemeId)

    // Dynamic theming local wrap
    AriaTheme(themeId = selectedThemeId) {
        Scaffold(
            containerColor = currentTheme.background,
            modifier = modifier.fillMaxSize()
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background artistic accents matching theme
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(currentTheme.primary.copy(alpha = 0.08f), Color.Transparent)
                            )
                        )
                        .align(Alignment.TopCenter)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header / LOGO
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "A R I A",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 6.sp,
                            color = currentTheme.primary
                        )
                        Text(
                            text = "SISTEMA DE FINANZAS DE LUJO",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = currentTheme.secondary,
                            fontSize = 9.sp
                        )
                    }

                    // STEP CONTROLS
                    when (step) {
                        1 -> OnboardingStepWelcome(
                            onNext = { step = 2 }
                        )
                        2 -> OnboardingStepCredentials(
                            username = username,
                            onUsernameChange = { username = it },
                            pin = pinField,
                            onPinChange = { pinField = it },
                            onNext = {
                                if (username.isBlank()) {
                                    errorMsg = "Por favor ingresa un nombre de usuario legítimo."
                                } else {
                                    errorMsg = ""
                                    step = 3
                                }
                            },
                            error = errorMsg
                        )
                        3 -> OnboardingStepFinance(
                            selectedCurrency = selectedCurrency,
                            onCurrencyChange = { selectedCurrency = it },
                            initialBalance = initialBalanceStr,
                            onBalanceChange = { initialBalanceStr = it },
                            onNext = {
                                step = 4
                            },
                            onBack = { step = 2 }
                        )
                        4 -> OnboardingStepThemes(
                            selectedThemeId = selectedThemeId,
                            onThemeSelect = { selectedThemeId = it },
                            onFinish = {
                                val balance = initialBalanceStr.toDoubleOrNull() ?: 0.0
                                viewModel.completeOnboarding(
                                    username = username,
                                    pin = pinField,
                                    initialBalance = balance,
                                    currency = selectedCurrency,
                                    activeThemeId = selectedThemeId
                                )
                            },
                            onBack = { step = 3 }
                        )
                    }

                    // Progress indicator row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        repeat(4) { idx ->
                            Box(
                                modifier = Modifier
                                    .width(if (step == idx + 1) 24.dp else 8.dp)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (step == idx + 1) currentTheme.primary else currentTheme.secondary.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStepWelcome(
    onNext: () -> Unit
) {
    val theme = AriaThemes.MINIMAL_WHITE // Default styled base object
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = theme.primary,
                modifier = Modifier.size(56.dp)
            )
            
            Text(
                text = "Tu Banco. Tu Control.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Aria opera completamente offline para resguardar con máxima privacidad tu dinero en carteras locales codificadas de primer nivel.",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.secondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("onboarding_welcome_next_button")
            ) {
                Text("Comenzar Registro", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingStepCredentials(
    username: String,
    onUsernameChange: (String) -> Unit,
    pin: String,
    onPinChange: (String) -> Unit,
    onNext: () -> Unit,
    error: String
) {
    val theme = LocalPremiumTheme.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Perfil Personal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.onBackground
            )

            Text(
                text = "Define tus credenciales de acceso local cifrado.",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.secondary
            )

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Nombre de usuario") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = theme.secondary.copy(alpha = 0.5f),
                    focusedLabelColor = theme.primary
                ),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_onboarding_input")
            )

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) onPinChange(it) },
                label = { Text("PIN de Acceso (4 números)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = theme.secondary.copy(alpha = 0.5f),
                    focusedLabelColor = theme.primary
                ),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("onboarding_creds_next_button")
            ) {
                Text("Continuar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingStepFinance(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    initialBalance: String,
    onBalanceChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val theme = LocalPremiumTheme.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ajustes de Moneda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.onBackground
            )

            Text(
                text = "Aria opera en base a USD (Dólares) o NIO (Córdobas). Selecciona tu preferencia principal.",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.secondary
            )

            // Currency Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCurrency == "USD") theme.primary else theme.secondary.copy(alpha = 0.1f))
                        .clickable { onCurrencyChange("USD") }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Dólar (USD $)",
                        color = if (selectedCurrency == "USD") theme.onPrimary else theme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCurrency == "NIO") theme.primary else theme.secondary.copy(alpha = 0.1f))
                        .clickable { onCurrencyChange("NIO") }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Córdoba (NIO C$)",
                        color = if (selectedCurrency == "NIO") theme.onPrimary else theme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedTextField(
                value = initialBalance,
                onValueChange = onBalanceChange,
                label = { Text("Fondos Iniciales (Saldo de Bóveda)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = theme.secondary.copy(alpha = 0.5f),
                    focusedLabelColor = theme.primary
                ),
                leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Atrás")
                }

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1.5f).height(50.dp)
                ) {
                    Text("Siguiente", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OnboardingStepThemes(
    selectedThemeId: String,
    onThemeSelect: (String) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val theme = LocalPremiumTheme.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Elige tu Estilo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = theme.onBackground
            )

            Text(
                text = "Lanza Aria en uno de nuestros 10 temas premium estilizados.",
                style = MaterialTheme.typography.bodySmall,
                color = theme.secondary
            )

            // Selectable Grid inside dynamic list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                AriaThemes.list.forEach { preTheme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedThemeId == preTheme.id) theme.primary.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (selectedThemeId == preTheme.id) theme.primary else Color.Gray.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onThemeSelect(preTheme.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Color color index indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(preTheme.background))
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(preTheme.surface))
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(preTheme.primary))
                        }

                        Text(
                            text = preTheme.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = theme.onBackground,
                            modifier = Modifier.weight(1f)
                        )

                        if (selectedThemeId == preTheme.id) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = theme.primary
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Atrás")
                }

                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("onboarding_complete_button")
                ) {
                    Text("Finalizar Ajustes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
