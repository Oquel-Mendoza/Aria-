package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.ui.theme.LocalPremiumTheme
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: FinanceViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val transactions by viewModel.transactions.collectAsState()

    // Determine calendar state
    val calendar = remember { Calendar.getInstance() }
    val currentMonth = remember { calendar.get(Calendar.MONTH) }
    val currentYear = remember { calendar.get(Calendar.YEAR) }

    val monthName = remember(currentMonth) {
        val sdf = SimpleDateFormat("MMMM 'de' yyyy", Locale("es", "ES"))
        sdf.format(calendar.time).replaceFirstChar { it.uppercase() }
    }

    // Days of the month breakdown
    val daysInMonth = remember(currentMonth, currentYear) {
        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeekIndex = remember(currentMonth, currentYear) {
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        // convert to 0-indexed starting on Monday (Lunes)
        // Calendar.SUNDAY = 1, MONDAY = 2, etc.
        val sundayStartIdx = tempCal.get(Calendar.DAY_OF_WEEK)
        val index = if (sundayStartIdx == Calendar.SUNDAY) 6 else sundayStartIdx - 2
        index
    }

    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    // Map transactions on selected day
    val selectedDayTransactions = remember(selectedDay, transactions) {
        transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            txCal.get(Calendar.YEAR) == currentYear &&
                    txCal.get(Calendar.MONTH) == currentMonth &&
                    txCal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(24.dp)
            .testTag("calendar_layer_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CALENDARIO CONTABLE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.primary
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.surface)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = theme.primary)
            }
        }

        // Header Month Picker Label
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(theme.surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.primary
            )
        }

        // Days of week Headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val daysLetter = listOf("L", "M", "M", "J", "V", "S", "D")
            daysLetter.forEach { letter ->
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.secondary,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Calendar Day Cells Grid (Max 6 rows of 7 cells)
        val cellsCount = firstDayOfWeekIndex + daysInMonth
        val rowsCount = (cellsCount + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            for (r in 0 until rowsCount) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (c in 0..6) {
                        val cellIdx = r * 7 + c
                        val dayNumber = cellIdx - firstDayOfWeekIndex + 1

                        if (dayNumber in 1..daysInMonth) {
                            val isSelected = dayNumber == selectedDay
                            
                            // Check if this single day has expenses / incomes loaded
                            val dayTxs = remember(dayNumber, transactions) {
                                transactions.filter { tx ->
                                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                                    txCal.get(Calendar.YEAR) == currentYear &&
                                            txCal.get(Calendar.MONTH) == currentMonth &&
                                            txCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                }
                            }
                            val hasExpense = dayTxs.any { it.type == "GASTO" }
                            val hasIncome = dayTxs.any { it.type == "INGRESO" }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) theme.primary else Color.Transparent
                                    )
                                    .clickable { selectedDay = dayNumber }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) theme.onPrimary else theme.onBackground
                                    )

                                    // Tiny transaction dots
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 1.dp)) {
                                        if (hasIncome) {
                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(theme.accent))
                                        }
                                        if (hasExpense) {
                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.Red))
                                        }
                                    }
                                }
                            }
                        } else {
                            // Blank cell padding
                            Box(modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = theme.secondary.copy(alpha = 0.15f))

        // Selected Day Ledger Lists
        Text(
            text = "DÍA %s DE %s".format(selectedDay, monthName.uppercase()),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = theme.secondary
        )

        if (selectedDayTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.EventNote, contentDescription = null, tint = theme.secondary, modifier = Modifier.size(36.dp))
                    Text(
                        text = "Sin registros para esta fecha.",
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.secondary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("calendar_day_lazy_list")
            ) {
                items(selectedDayTransactions) { tx ->
                    val isExpense = tx.type == "GASTO"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = tx.note.ifBlank { tx.category },
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onBackground,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = tx.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.secondary,
                                    fontSize = 10.sp
                                )
                            }

                            Text(
                                text = if (isExpense) "-$ %.2f".format(tx.amount) else "+$ %.2f".format(tx.amount),
                                fontWeight = FontWeight.Black,
                                color = if (isExpense) Color.Red else theme.accent,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
