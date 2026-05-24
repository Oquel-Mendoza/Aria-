package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Goal
import com.example.data.Wallet
import com.example.ui.theme.LocalPremiumTheme
import com.example.ui.widgets.SavingGoalProgressRing
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val goals by viewModel.goals.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val hideBalances = userProfile?.hideBalances ?: false

    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var activeContributionGoal by remember { mutableStateOf<Goal?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
            .testTag("savings_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AHORROS Y METAS",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = theme.primary,
                fontSize = 24.sp,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = { showCreateGoalDialog = true },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva Meta",
                    tint = theme.onPrimary
                )
            }
        }

        // List layout
        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CrisisAlert,
                        contentDescription = null,
                        tint = theme.secondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Establece objetivos de ahorro de corto o mediano plazo.",
                        color = theme.secondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                goals.forEach { goal ->
                    val ratio = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
                    val sdf = remember { SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES")) }
                    val targetFormatted = remember(goal.targetDate) { sdf.format(Date(goal.targetDate)) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surface),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Saving arc ring visual component
                            SavingGoalProgressRing(
                                progress = ratio,
                                colorHex = goal.colorHex,
                                modifier = Modifier.size(64.dp)
                            )

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = goal.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = theme.onBackground
                                )

                                Text(
                                    text = "Objetivo: $${goal.targetAmount} USD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.secondary
                                )

                                if (goal.isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(theme.accent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "¡COMPLETADA! 🏆",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 8.sp,
                                            color = theme.accent
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Límite: $targetFormatted",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = theme.secondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Contribution Action button
                            if (!goal.isCompleted) {
                                Button(
                                    onClick = { activeContributionGoal = goal },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Aportar", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                IconButton(onClick = { viewModel.deleteGoal(goal) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = theme.secondary.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog overlays
    if (showCreateGoalDialog) {
        CreateGoalModal(
            wallets = wallets,
            onClose = { showCreateGoalDialog = false },
            onSubmit = { name, target, date, walletId, colorHex ->
                viewModel.createGoal(name, target, date, walletId, "sports_target", colorHex)
                showCreateGoalDialog = false
            }
        )
    }

    if (activeContributionGoal != null) {
        ContributeGoalModal(
            goal = activeContributionGoal!!,
            wallets = wallets,
            onClose = { activeContributionGoal = null },
            onSubmit = { amount, walletId ->
                viewModel.contributeGoal(activeContributionGoal!!, amount, walletId)
                activeContributionGoal = null
            }
        )
    }
}

@Composable
fun CreateGoalModal(
    wallets: List<Wallet>,
    onClose: () -> Unit,
    onSubmit: (name: String, target: Double, date: Long, walletId: Long, colorHex: String) -> Unit
) {
    val theme = LocalPremiumTheme.current

    var name by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: 0L) }
    var selectedColor by remember { mutableStateOf("#10B981") } // Mint default

    var errorMsg by remember { mutableStateOf("") }
    val colors = listOf("#10B981", "#3B82F6", "#EC4899", "#F59E0B", "#F43F5E")

    Dialog(onDismissRequest = onClose) {
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CREAR META DE AHORRO 🎯",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = theme.primary
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = theme.secondary)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la meta de ahorro") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, focusedLabelColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("goal_name_input")
                )

                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Monto Objetivo ($ USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, focusedLabelColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("goal_target_input")
                )

                // Select Linked Account
                Text(
                    text = "Vincular Cuenta de Fondos",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.secondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    wallets.forEach { w ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedWalletId == w.id) theme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { selectedWalletId = w.id }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = w.name, fontWeight = FontWeight.Bold, color = theme.onBackground, style = MaterialTheme.typography.bodySmall)
                            Text(text = "${w.balance} ${w.currency}", color = theme.secondary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Choose visual color
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    colors.forEach { col ->
                        val parsed = Color(android.graphics.Color.parseColor(col))
                        val isSel = selectedColor == col
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(parsed)
                                .clickable { selectedColor = col }
                                .padding(4.dp)
                        ) {
                            if (isSel) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
                            }
                        }
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val target = targetStr.toDoubleOrNull() ?: 0.0
                        if (name.isBlank() || target <= 0.0) {
                            errorMsg = "Por favor ingresa datos óptimos."
                        } else {
                            // Meta date targets set to 6 months by default
                            val halfYearAway = System.currentTimeMillis() + (180L * 24L * 60L * 60L * 1000L)
                            onSubmit(name, target, halfYearAway, selectedWalletId, selectedColor)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("goal_submit_button")
                ) {
                    Text("Establecer Hito", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ContributeGoalModal(
    goal: Goal,
    wallets: List<Wallet>,
    onClose: () -> Unit,
    onSubmit: (amount: Double, walletId: Long) -> Unit
) {
    val theme = LocalPremiumTheme.current
    var amountStr by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(goal.walletId) }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onClose) {
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APORTAR A META 🏆",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = theme.primary
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = theme.secondary)
                    }
                }

                Text(
                    text = "¿Cuánto deseas transferir hoy a tu meta de ahorro '${goal.name}'?",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.secondary
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Suma de aporte") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, focusedLabelColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("contribution_amount_input")
                )

                // Choose payment cards source
                Text(
                    text = "Abonar desde Cuenta:",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.secondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    wallets.forEach { w ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedWalletId == w.id) theme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { selectedWalletId = w.id }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = w.name, fontWeight = FontWeight.Bold, color = theme.onBackground, style = MaterialTheme.typography.bodySmall)
                            Text(text = "${w.balance} ${w.currency}", color = theme.secondary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        val chosenW = wallets.firstOrNull { it.id == selectedWalletId }
                        if (amount <= 0.0) {
                            errorMsg = "Por favor, ingresa un importe legítimo."
                        } else if (chosenW != null && chosenW.balance < amount) {
                            errorMsg = "Fondos insuficientes en la cuenta '${chosenW.name}'."
                        } else {
                            onSubmit(amount, selectedWalletId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("contribution_submit_button")
                ) {
                    Text("Abonar Fondos", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
