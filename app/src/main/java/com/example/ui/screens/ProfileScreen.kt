package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalPremiumTheme
import com.example.viewmodel.FinanceViewModel

@Composable
fun ProfileScreen(
    viewModel: FinanceViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val userProfile by viewModel.userProfile.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val goals by viewModel.goals.collectAsState()

    val username = userProfile?.username ?: "Usuario"
    val preferredCurrency = userProfile?.preferredCurrency ?: "USD"

    // Analytics calculations
    val totalBalance = remember(wallets) { wallets.sumOf { it.balance } }
    val totalTransactions = remember(transactions) { transactions.size }
    val completedGoals = remember(goals) { goals.count { it.isCompleted } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("profile_layer_container"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PERFIL FINANCIERO",
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

        // Profile Avatar Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(theme.primary.copy(alpha = 0.15f))
                    .border(2.dp, theme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(2).uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = theme.primary
                )
            }

            Text(
                text = username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = theme.onBackground
            )

            Text(
                text = "Miembro Distinguido de Aria",
                style = MaterialTheme.typography.labelSmall,
                color = theme.secondary
            )
        }

        // Grid of micro financial metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricBadgeCard(
                title = "FONDOS",
                value = "$%.1f %s".format(totalBalance, preferredCurrency),
                icon = Icons.Default.Savings,
                modifier = Modifier.weight(1f)
            )

            MetricBadgeCard(
                title = "REGISTROS",
                value = "$totalTransactions",
                icon = Icons.Default.HistoryToggleOff,
                modifier = Modifier.weight(1f)
            )
        }

        // LOGROS / UNLOCKED REWARDS FINTECH ACCENTS
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "INSIGNIAS Y RENDIMIENTO ARIA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = theme.secondary
                )

                // List of mocked achievements
                val achieves = listOf(
                    Triple("Bóveda Segura 🔒", "Has habilitado un código PIN para bloquear de forma segura tu dinero local.", userProfile?.pin?.isNotEmpty() ?: false),
                    Triple("Ahorrador Inteligente 🎯", "Has completado al menos un hito de ahorro en Aria.", completedGoals > 0),
                    Triple("Libro de Actividades 📒", "Registraste tus primeros 5 movimientos financieros.", totalTransactions >= 5),
                    Triple("Multi-carteras 💳", "Has configurado más de un monedero en paralelo en tu bóveda.", wallets.size > 1)
                )

                achieves.forEach { (title, subtitle, unlocked) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (unlocked) theme.primary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (unlocked) theme.primary else theme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f).wrapContentHeight()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (unlocked) theme.onBackground else theme.secondary
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.secondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBadgeCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = theme.primary)
            Text(title, style = MaterialTheme.typography.labelSmall, color = theme.secondary)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}
