package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.Wallet
import com.example.ui.theme.LocalPremiumTheme

@Composable
fun AddTransactionModalDialog(
    type: String, // GASTO, INGRESO, TRANSFERENCIA
    availableWallets: List<Wallet>,
    onClose: () -> Unit,
    onSubmit: (amount: Double, walletId: Long, destWalletId: Long?, category: String, note: String, tags: String) -> Unit
) {
    val theme = LocalPremiumTheme.current

    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(availableWallets.firstOrNull()?.id ?: 0L) }
    var selectedDestWalletId by remember { mutableStateOf(availableWallets.getOrNull(1)?.id ?: 0L) }
    var tags by remember { mutableStateOf("") }

    // Categories list based on movement type
    val categoriesExp = listOf("Comida", "Transporte", "Compras", "Entretenimiento", "Facturas", "Educación", "Salud", "Otros")
    val categoriesInc = listOf("Salario", "Depósitos", "Inversiones", "Otros")
    val categories = if (type == "INGRESO") categoriesInc else categoriesExp
    var selectedCategory by remember { mutableStateOf(categories.first()) }

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
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (type) {
                            "GASTO" -> "REGISTRAR GASTO 💔"
                            "INGRESO" -> "AÑADIR INGRESO 💚"
                            else -> "NUEVA TRANSFERENCIA 🔃"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = theme.primary
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = theme.secondary)
                    }
                }

                // Amount Text Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Monto ($ / C$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, focusedLabelColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("tx_amount_input")
                )

                // Source Wallet select
                Text(
                    text = "Selecciona la Cuenta Origen",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.secondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    availableWallets.forEach { w ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedWalletId == w.id) theme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { selectedWalletId = w.id }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = w.name, fontWeight = FontWeight.Bold, color = theme.onBackground)
                            Text(text = "${w.balance} ${w.currency}", color = theme.secondary)
                        }
                    }
                }

                // If Transfer, dest wallet select
                if (type == "TRANSFERENCIA") {
                    Text(
                        text = "Selecciona la Cuenta Destino",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.secondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableWallets.filter { it.id != selectedWalletId }.forEach { w ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedDestWalletId == w.id) theme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { selectedDestWalletId = w.id }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = w.name, fontWeight = FontWeight.Bold, color = theme.onBackground)
                                Text(text = "${w.balance} ${w.currency}", color = theme.secondary)
                            }
                        }
                    }
                }

                // Category select
                if (type != "TRANSFERENCIA") {
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.secondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .verticalScroll(rememberScrollState())
                            .wrapContentHeight()
                    ) {
                        categories.forEach { catName ->
                            val isSel = selectedCategory == catName
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp, bottom = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) theme.primary else theme.secondary.copy(alpha = 0.15f))
                                    .clickable { selectedCategory = catName }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catName,
                                    color = if (isSel) theme.onPrimary else theme.onBackground,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota de descripción") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, focusedLabelColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("tx_note_input")
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        if (amount <= 0.0) {
                            errorMsg = "Por favor, ingresa un importe numérico mayor a cero."
                        } else {
                            val finalCat = if (type == "TRANSFERENCIA") "Transferencia" else selectedCategory
                            val finalDestId = if (type == "TRANSFERENCIA") selectedDestWalletId else null
                            onSubmit(amount, selectedWalletId, finalDestId, finalCat, note, tags)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("tx_submit_button")
                ) {
                    Text("Guardar Registro", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddWalletModalDialog(
    onClose: () -> Unit,
    onSubmit: (name: String, type: String, icon: String, color: String, balance: Double, currency: String) -> Unit
) {
    val theme = LocalPremiumTheme.current

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("BANCO") } // EFECTIVO, BANCO, AHORROS, EMERGENCIA
    var balanceStr by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var selectedColorHex by remember { mutableStateOf("#3B82F6") } // Sky blue default

    val availableColors = listOf("#3B82F6", "#10B981", "#EC4899", "#8B5CF6", "#F59E0B", "#F43F5E")
    val types = listOf("BANCO", "EFECTIVO", "AHORROS", "EMERGENCIA")

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
                        text = "NUEVA TARJETA / CUENTA 💳",
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
                    label = { Text("Nombre de la cuenta (ej. Bac Credomatic)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, focusedLabelColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("wallet_name_input")
                )

                // Type select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types.forEach { t ->
                        val isSel = selectedType == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) theme.primary else theme.secondary.copy(alpha = 0.15f))
                                .clickable { selectedType = t }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t,
                                color = if (isSel) theme.onPrimary else theme.onBackground,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = balanceStr,
                    onValueChange = { balanceStr = it },
                    label = { Text("Saldo inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, focusedLabelColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("wallet_balance_input")
                )

                // Currency select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedCurrency == "USD") theme.primary else theme.secondary.copy(alpha = 0.1f))
                            .clickable { selectedCurrency = "USD" }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("USD ($)", color = if (selectedCurrency == "USD") theme.onPrimary else theme.onBackground, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedCurrency == "NIO") theme.primary else theme.secondary.copy(alpha = 0.1f))
                            .clickable { selectedCurrency = "NIO" }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("NIO (C$)", color = if (selectedCurrency == "NIO") theme.onPrimary else theme.onBackground, fontWeight = FontWeight.Bold)
                    }
                }

                // Color selectors
                Text("Color de Tarjeta", style = MaterialTheme.typography.labelSmall, color = theme.secondary, modifier = Modifier.align(Alignment.Start))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableColors.forEach { col ->
                        val parsedCol = Color(android.graphics.Color.parseColor(col))
                        val isSel = selectedColorHex == col
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(parsedCol)
                                .clickable { selectedColorHex = col }
                                .padding(4.dp)
                        ) {
                            if (isSel) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val bal = balanceStr.toDoubleOrNull() ?: 0.0
                        if (name.isBlank()) {
                            errorMsg = "Por favor ingresa un nombre para la cuenta."
                        } else {
                            onSubmit(name, selectedType, "account_balance", selectedColorHex, bal, selectedCurrency)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("wallet_submit_button")
                ) {
                    Text("Crear Tarjeta", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
