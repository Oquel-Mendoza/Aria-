package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.ui.theme.LocalPremiumTheme
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val transactions by viewModel.transactions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val hideBalances = userProfile?.hideBalances ?: false

    // Interactive search terms local flow
    var queryText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("TODAS") } // TODAS, INGRESO, GASTO, TRANSFERENCIA
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Categories available for filtering
    val categories = remember(transactions) {
        listOf("Comida", "Transporte", "Compras", "Entretenimiento", "Facturas", "Educación", "Salud", "Ahorros", "Salario")
    }

    // Filter logic
    val filteredTransactions = remember(transactions, queryText, selectedType, selectedCategory) {
        transactions.filter { tx ->
            val matchQuery = queryText.isBlank() || 
                    tx.note.contains(queryText, ignoreCase = true) || 
                    tx.category.contains(queryText, ignoreCase = true)
            
            val matchType = selectedType == "TODAS" || tx.type == selectedType
            
            val matchCategory = selectedCategory == null || tx.category == selectedCategory

            matchQuery && matchType && matchCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP Header
        Text(
            text = "MOVIMIENTOS",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = theme.primary,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
            fontSize = 24.sp,
            letterSpacing = 1.sp
        )

        // Search Textbox
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            placeholder = { Text("Buscar por nota o concepto...", color = theme.secondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.primary) },
            trailingIcon = {
                if (queryText.isNotEmpty()) {
                    IconButton(onClick = { queryText = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = theme.secondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = theme.surface.copy(alpha = 0.5f),
                unfocusedContainerColor = theme.surface.copy(alpha = 0.3f),
                focusedBorderColor = theme.primary,
                unfocusedBorderColor = theme.secondary.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .testTag("transactions_search_bar")
        )

        // Filters: Type Row Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val typeFilters = listOf("TODAS", "INGRESO", "GASTO", "TRANSFERENCIA")
            val labels = listOf("Todo", "Ingresos", "Gastos", "Transf.")

            typeFilters.zip(labels).forEach { (typeKey, label) ->
                val isSel = selectedType == typeKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) theme.primary else theme.surface)
                        .clickable { selectedType = typeKey }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSel) theme.onPrimary else theme.onBackground,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Category Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val isAllSel = selectedCategory == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAllSel) theme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { selectedCategory = null }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Todas las categorías",
                        color = if (isAllSel) theme.primary else theme.secondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(categories) { catName ->
                val isSel = selectedCategory == catName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) theme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { selectedCategory = catName }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        catName,
                        color = if (isSel) theme.primary else theme.secondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Transactions scrolling list
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = theme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Ningún movimiento coincide con los filtros actuales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.secondary
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().testTag("transactions_lazy_list")
            ) {
                items(filteredTransactions) { tx ->
                    val sdf = remember { SimpleDateFormat("dd 'de' MMM, yyyy - hh:mm a", Locale("es", "ES")) }
                    val dateFormatted = remember(tx.date) { sdf.format(Date(tx.date)) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (tx.type) {
                                            "GASTO" -> Color.Red.copy(alpha = 0.1f)
                                            "INGRESO" -> theme.accent.copy(alpha = 0.1f)
                                            else -> theme.primary.copy(alpha = 0.1f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (tx.type) {
                                        "GASTO" -> Icons.Default.Clear
                                        "INGRESO" -> Icons.Default.ArrowDownward
                                        else -> Icons.Default.SwapHoriz
                                    },
                                    contentDescription = null,
                                    tint = when (tx.type) {
                                        "GASTO" -> Color.Red
                                        "INGRESO" -> theme.accent
                                        else -> theme.primary
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.note.ifBlank { tx.category },
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onBackground,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "$dateFormatted | ${tx.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.secondary,
                                    fontSize = 10.sp
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (hideBalances) "•••" else {
                                        val prefix = if (tx.type == "GASTO") "-" else "+"
                                        "$prefix %.2f %s".format(tx.amount, tx.currency)
                                    },
                                    fontWeight = FontWeight.Black,
                                    color = if (tx.type == "GASTO") Color.Red else theme.accent,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                IconButton(
                                    onClick = { viewModel.deleteTransaction(tx) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Borrar",
                                        tint = theme.secondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(12.dp)
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
