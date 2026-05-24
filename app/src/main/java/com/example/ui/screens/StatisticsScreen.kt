package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalPremiumTheme
import com.example.ui.widgets.PremiumDonutChart
import com.example.ui.widgets.SmoothLineChart
import com.example.viewmodel.FinanceViewModel
import java.util.*

@Composable
fun StatisticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val transactions by viewModel.transactions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val hideBalances = userProfile?.hideBalances ?: false

    // Compute stats metrics dynamically
    val categoryTotals = remember(transactions) {
        val expenses = transactions.filter { it.type == "GASTO" }
        val totals = mutableMapOf<String, Float>()
        for (tx in expenses) {
            val total = totals.getOrDefault(tx.category, 0f)
            totals[tx.category] = total + tx.amount.toFloat()
        }
        totals
    }

    // Colors mapping for category graph slices
    val colorPalette = listOf(
        Color(0xFFFF5722), // Comida -> Orange
        Color(0xFF2196F3), // Transporte -> Blue
        Color(0xFF9C27B0), // Compras -> Purple
        Color(0xFFE91E63), // Entretenimiento -> Pink
        Color(0xFFF44336), // Facturas -> Red
        Color(0xFF3F51B5), // Educación -> Indigo
        Color(0xFF4CAF50), // Salud -> Green
        Color(0xFF607D8B), // Otros -> Blue Grey
        Color(0xFF009688)  // Ahorro -> Teal
    )

    val chartSlices = remember(categoryTotals) {
        var idx = 0
        categoryTotals.map { (cat, sum) ->
            val col = colorPalette[idx % colorPalette.size]
            idx++
            Pair(sum, col)
        }
    }

    // Trend plot indices (take daily expense increments or raw last 7 transactions sums)
    val trendPoints = remember(transactions) {
        val expenses = transactions.filter { it.type == "GASTO" }.take(10).reversed()
        if (expenses.isEmpty()) {
            listOf(0f, 0f)
        } else {
            expenses.map { it.amount.toFloat() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
            .testTag("statistics_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP Header
        Text(
            text = "ESTADÍSTICAS",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = theme.primary,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
            fontSize = 24.sp,
            letterSpacing = 1.sp
        )

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = theme.secondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Registra movimientos para habilitar métricas inteligentes.",
                        color = theme.secondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Trend Spline Curve Chart Card
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURVA DE EXCESOS & EGRESOS",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = theme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = theme.primary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Embedded bezier graphics wrapper
                    SmoothLineChart(
                        points = trendPoints,
                        lineColor = theme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "La gráfica refleja de forma suavizada tus últimos 10 desembolsos registrados.",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.secondary
                    )
                }
            }

            // Donut breakdown card
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "DISTRIBUCIÓN DE GASTOS",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        color = theme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    if (categoryTotals.isEmpty()) {
                        Text(
                            text = "Sube registros de gastos para dibujar la gráfica pastel.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.secondary
                        )
                    } else {
                        // Rendering Pie Chart
                        PremiumDonutChart(
                            slices = chartSlices,
                            modifier = Modifier.size(160.dp)
                        )

                        // Legend Listing vertical
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var colorIdx = 0
                            categoryTotals.forEach { (catName, sum) ->
                                val color = colorPalette[colorIdx % colorPalette.size]
                                colorIdx++

                                val grandExpenseTotal = categoryTotals.values.sum()
                                val pct = (sum / grandExpenseTotal) * 100

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(
                                            text = catName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.onBackground
                                        )
                                    }

                                    Text(
                                        text = if (hideBalances) "•••" else "%.0f%% (%.1f $)".format(pct, sum),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = theme.secondary
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
