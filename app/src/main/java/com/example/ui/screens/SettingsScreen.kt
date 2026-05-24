package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AriaThemes
import com.example.ui.theme.LocalPremiumTheme
import com.example.viewmodel.FinanceViewModel

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()

    var customExchangeRateStr by remember { mutableStateOf(userProfile?.usdToNioRate?.toString() ?: "36.5") }
    var newPinField by remember { mutableStateOf("") }
    
    // Backup copy paste overlays
    var backupJsonString by remember { mutableStateOf("") }
    var restoreJsonString by remember { mutableStateOf("") }
    var showBackupArea by remember { mutableStateOf(false) }
    var showRestoreArea by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp) // Offset for bottom nav
            .testTag("settings_screen_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP Header
        Text(
            text = "CONFIGURACIÓN",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = theme.primary,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
            fontSize = 24.sp,
            letterSpacing = 1.sp
        )

        // 1. SELECTOR DE TEMAS PREMIUM (10 ITEMS)
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = theme.primary)
                    Text(
                        text = "TEMAS EXCLUSIVOS DE ARIA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = theme.secondary
                    )
                }

                Text(
                    text = "Personaliza el aspecto de tu interfaz bancaria de forma instantánea:",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.secondary
                )

                // Themes Vertical map
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AriaThemes.list.forEach { curTheme ->
                        val isSelected = curTheme.id == userProfile?.activeThemeId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) theme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) theme.primary else Color.Gray.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.updateTheme(curTheme.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Style Indicators
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(curTheme.background))
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(curTheme.surface))
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(curTheme.primary))
                            }

                            Text(
                                text = curTheme.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = theme.onBackground,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Activo",
                                    tint = theme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. EXCHANGE RATIO EDITOR & SECURITY OVERRIDES
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header manual conversion
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = theme.primary)
                    Text(
                        text = "DIVISAS Y TASA DE CAMBIO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = theme.secondary
                    )
                }

                OutlinedTextField(
                    value = customExchangeRateStr,
                    onValueChange = { customExchangeRateStr = it },
                    label = { Text("Tasa (1 USD = NIO)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, focusedLabelColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("exchange_rate_input")
                )

                Button(
                    onClick = {
                        val rate = customExchangeRateStr.toDoubleOrNull() ?: 36.5
                        viewModel.updateExchangeRate(rate)
                        Toast.makeText(context, "Tasa de cambio actualizada!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar Tasa de Cambio", fontWeight = FontWeight.Bold)
                }

                Divider(color = theme.secondary.copy(alpha = 0.15f))

                // Security overrides
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = theme.primary)
                    Text(
                        text = "SEGURIDAD CIFRADA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = theme.secondary
                    )
                }

                OutlinedTextField(
                    value = newPinField,
                    onValueChange = { if (it.length <= 4) newPinField = it },
                    label = { Text("Nuevo PIN de Acceso (4 dígitos)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary, focusedLabelColor = theme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pin_override_input")
                )

                Button(
                    onClick = {
                        if (newPinField.length == 4) {
                            viewModel.changePinCode(newPinField)
                            newPinField = ""
                            Toast.makeText(context, "PIN de Acceso Modificado!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Escribe exactamente 4 dígitos.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reconfigurar Código PIN", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. COPIAS DE SEGURIDAD / PROCEDURES RESPALDO
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = theme.primary)
                    Text(
                        text = "CUSTODIA DE SEGURIDAD Y EXPORTACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = theme.secondary
                    )
                }

                Text(
                    text = "Exporta tus informes locales a PDF o guarda respaldos codificados de base de datos JSON.",
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.secondary
                )

                // Row action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.getBackupString { json ->
                                backupJsonString = json
                                showBackupArea = true
                                Toast.makeText(context, "Copia de Seguridad JSON generada!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("backup_json_trigger")
                    ) {
                        Text("Crear Respaldo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }

                    Button(
                        onClick = {
                            viewModel.simulatePdfReport { file ->
                                Toast.makeText(context, "PDF Reporte guardado en: ${file.name}", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("backup_pdf_trigger")
                    ) {
                        Text("Exportar PDF", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }

                if (showBackupArea) {
                    OutlinedTextField(
                        value = backupJsonString,
                        onValueChange = {},
                        label = { Text("Texto de Copia (Copia este String)") },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary),
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }

                Divider(color = theme.secondary.copy(alpha = 0.15f))

                Button(
                    onClick = { showRestoreArea = !showRestoreArea },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surface, contentColor = theme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restaurar desde JSON", fontWeight = FontWeight.Bold)
                }

                if (showRestoreArea) {
                    OutlinedTextField(
                        value = restoreJsonString,
                        onValueChange = { restoreJsonString = it },
                        placeholder = { Text("Pega aquí tu código JSON de Aria anterior...") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.primary),
                        modifier = Modifier.fillMaxWidth().height(120.dp).testTag("restore_json_input")
                    )

                    Button(
                        onClick = {
                            viewModel.restoreBackupString(restoreJsonString) { success ->
                                if (success) {
                                    Toast.makeText(context, "Base de datos restaurada con éxito!", Toast.LENGTH_SHORT).show()
                                    restoreJsonString = ""
                                    showRestoreArea = false
                                } else {
                                    Toast.makeText(context, "Error al restaurar: JSON Corrupto.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("restore_json_submit")
                    ) {
                        Text("Sincronizar Respaldo", fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = theme.secondary.copy(alpha = 0.15f))

                // Logout button
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bloquear y Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
