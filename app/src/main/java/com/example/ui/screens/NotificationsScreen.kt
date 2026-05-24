package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalPremiumTheme
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    viewModel: FinanceViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val notifications by viewModel.notifications.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(24.dp)
            .testTag("notifications_layer_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AVISOS INTELIGENTES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.primary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (notifications.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearNotifications() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Limpiar Todo", tint = Color.Red)
                        }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(theme.surface)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = theme.primary)
                }
            }
        }

        Text(
            text = "Análisis en tiempo real de tus hábitos de gasto y metas de ahorro:",
            style = MaterialTheme.typography.bodySmall,
            color = theme.secondary
        )

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = theme.secondary, modifier = Modifier.size(48.dp))
                    Text(text = "Tu buzón de alertas está impecable.", color = theme.secondary)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("notifications_lazy_list")
            ) {
                items(notifications) { msg ->
                    val dateStr = remember(msg.date) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
                        sdf.format(Date(msg.date))
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (msg.typeString) {
                                            "ALERTA" -> Color.Red.copy(alpha = 0.15f)
                                            "META" -> theme.primary.copy(alpha = 0.15f)
                                            else -> theme.secondary.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (msg.typeString) {
                                        "ALERTA" -> Icons.Default.Warning
                                        "META" -> Icons.Default.EmojiEvents
                                        else -> Icons.Default.TipsAndUpdates
                                    },
                                    contentDescription = null,
                                    tint = when (msg.typeString) {
                                        "ALERTA" -> Color.Red
                                        "META" -> theme.primary
                                        else -> theme.secondary
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = msg.title,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onBackground,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp
                                )

                                Text(
                                    text = msg.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.secondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )

                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = theme.secondary.copy(alpha = 0.6f),
                                    fontSize = 9.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteNotification(msg.id) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cerrar",
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
