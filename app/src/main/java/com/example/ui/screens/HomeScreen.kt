package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.Wallet
import com.example.ui.theme.LocalPremiumTheme
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val userProfile by viewModel.userProfile.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val goals by viewModel.goals.collectAsState()

    val preferredCurrency = userProfile?.preferredCurrency ?: "USD"
    val hideBalances = userProfile?.hideBalances ?: false
    val rate = userProfile?.usdToNioRate ?: 36.5

    // Calculamos balance total
    val totalBalance = remember(wallets, preferredCurrency, rate) {
        var sum = 0.0
        for (w in wallets) {
            if (w.currency == preferredCurrency) {
                sum += w.balance
            } else {
                if (preferredCurrency == "USD" && w.currency == "NIO") {
                    sum += w.balance / rate
                } else if (preferredCurrency == "NIO" && w.currency == "USD") {
                    sum += w.balance * rate
                }
            }
        }
        sum
    }

    // Dynamic hour greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    val todayStr = remember {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        sdf.format(Date()).replaceFirstChar { it.uppercase() }
    }

    // Dialog sheets states
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var transactionDialogType by remember { mutableStateOf("GASTO") } // GASTO, INGRESO, TRANSFERENCIA
    var showAddWalletDialog by remember { mutableStateOf(false) }

    // List of simulated frozen credit cards (stored as a list of customized widget card states locally)
    var frozenCardsSet by remember { mutableStateOf(setOf<Long>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp) // Safety margin for navigation bar
    ) {
        // Dynamic greeting & date
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$greeting, ${userProfile?.username ?: "Usuario"} ✨",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = theme.onBackground
                    )
                    Text(
                        text = todayStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = theme.secondary
                    )
                }

                // Balance Visibility Toggle
                IconButton(
                    onClick = { viewModel.toggleHideBalances() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(theme.surface)
                ) {
                    Icon(
                        imageVector = if (hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Ocultar Balance",
                        tint = theme.primary
                    )
                }
            }
        }

        // Financial summary sheet
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .border(
                    width = if (theme.luxuryGoldBorder) 1.dp else 0.dp,
                    color = if (theme.luxuryGoldBorder) theme.primary.copy(alpha = 0.4f) else Color.Transparent,
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "BALANCE DE BÓVEDA TOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = theme.secondary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hideBalances) "••••••" else {
                                if (preferredCurrency == "USD") {
                                    "$ %s USD".format(Locale.US, String.format("%.2f", totalBalance))
                                } else {
                                    "C$ %s NIO".format(Locale.US, String.format("%.2f", totalBalance))
                                }
                            },
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = theme.primary
                        )
                    }

                    // Income vs Expense quick metric rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Quick info indicators
                        val tIncome = remember(transactions) {
                            transactions.filter { it.type == "INGRESO" }.sumOf { it.amount }
                        }
                        val tExpense = remember(transactions) {
                            transactions.filter { it.type == "GASTO" }.sumOf { it.amount }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = theme.background.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(theme.accent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = theme.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text("Ingresos", style = MaterialTheme.typography.labelSmall, color = theme.secondary)
                                    Text(
                                        text = if (hideBalances) "•••" else "$%.1f".format(tIncome),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.onBackground
                                    )
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = theme.background.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text("Egresos", style = MaterialTheme.typography.labelSmall, color = theme.secondary)
                                    Text(
                                        text = if (hideBalances) "•••" else "$%.1f".format(tExpense),
                                        style = MaterialTheme.typography.bodyMedium,
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

        // QUICK ACTIONS SHORTCUT ROW
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ACCIONES DEL BANCO",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.2.sp,
                color = theme.secondary,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val actionButtons = listOf(
                    Triple("Gasto", Icons.Default.ArrowOutward, "GASTO"),
                    Triple("Ingreso", Icons.Default.VerticalAlignBottom, "INGRESO"),
                    Triple("Transferir", Icons.Default.CompareArrows, "TRANSFERENCIA"),
                    Triple("Cartera", Icons.Default.Add, "CARTERA")
                )

                for ((label, icon, typeKey) in actionButtons) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clickable {
                                if (typeKey == "CARTERA") {
                                    showAddWalletDialog = true
                                } else {
                                    transactionDialogType = typeKey
                                    showAddTransactionDialog = true
                                }
                            }
                            .testTag("shortcut_$typeKey")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(theme.surface)
                                .border(
                                    0.5.dp,
                                    theme.secondary.copy(alpha = 0.15f),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = theme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = theme.onBackground
                        )
                    }
                }
            }
        }

        // WALL CARDS/BILLETERAS SCROLL VIEW
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MIS TARJETAS DIGITALES",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = theme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${wallets.size} Activas",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary
                )
            }

            if (wallets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = theme.secondary, modifier = Modifier.size(36.dp))
                        Text(
                            text = "No has registrado ninguna cuenta bancaria aún.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.secondary
                        )
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(wallets) { wallet ->
                        val isCardFrozen = frozenCardsSet.contains(wallet.id)
                        MockFintechCard(
                            wallet = wallet,
                            hideBalance = hideBalances,
                            frozen = isCardFrozen,
                            onFreezeChanged = { frozen ->
                                frozenCardsSet = if (frozen) {
                                    frozenCardsSet + wallet.id
                                } else {
                                    frozenCardsSet - wallet.id
                                }
                            }
                        )
                    }
                }
            }
        }

        // RECENT MOVEMENTS LIST
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MOVIMIENTOS RECIENTES",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    color = theme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Ver Todos",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary,
                    modifier = Modifier.clickable { onNavigateToTransactions() }
                )
            }

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no hay transacciones reportadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.secondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    transactions.take(4).forEach { tx ->
                        TransactionItemCard(
                            tx = tx,
                            hideBalance = hideBalances,
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    // MODAL DIALOGS INSIDE THE FLOW FOR FULL FUNCTIONAL WRITING
    if (showAddTransactionDialog) {
        AddTransactionModalDialog(
            type = transactionDialogType,
            availableWallets = wallets,
            onClose = { showAddTransactionDialog = false },
            onSubmit = { amount, walletId, destId, category, note, tags ->
                viewModel.createTransaction(
                    type = transactionDialogType,
                    amount = amount,
                    currency = wallets.firstOrNull { it.id == walletId }?.currency ?: "USD",
                    walletId = walletId,
                    destWalletId = destId,
                    category = category,
                    note = note,
                    tags = tags,
                    isRec = false,
                    recType = "NINGUNO"
                )
                showAddTransactionDialog = false
            }
        )
    }

    if (showAddWalletDialog) {
        AddWalletModalDialog(
            onClose = { showAddWalletDialog = false },
            onSubmit = { name, type, icon, color, balance, currency ->
                viewModel.createWallet(name, type, icon, color, balance, currency)
                showAddWalletDialog = false
            }
        )
    }
}

@Composable
fun MockFintechCard(
    wallet: Wallet,
    hideBalance: Boolean,
    frozen: Boolean,
    onFreezeChanged: (Boolean) -> Unit
) {
    val theme = LocalPremiumTheme.current

    // Parse color custom hex safely
    val cardColor = remember(wallet.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(wallet.colorHex))
        } catch (e: Exception) {
            theme.primary
        }
    }

    // Card background visual options
    val brush = remember(cardColor, frozen) {
        if (frozen) {
            Brush.linearGradient(
                colors = listOf(Color(0xFF555555), Color(0xFF222222))
            )
        } else {
            Brush.linearGradient(
                colors = listOf(cardColor, cardColor.copy(alpha = 0.7f), Color.Black)
            )
        }
    }

    Box(
        modifier = Modifier
            .width(310.dp)
            .height(190.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(brush)
            .border(
                1.dp,
                if (theme.luxuryGoldBorder) theme.primary else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        // Card mesh details
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle digital arcs
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 120.dp.toPx(),
                center = Offset(size.width, 0f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Wallet Name & Bank chip graphic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = wallet.name.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = wallet.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }

                // SIMULATOR BRANDING / NFC ICON / CHIP GRAPHIC
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // lines on chip
                        drawLine(Color.White.copy(alpha = 0.4f), Offset(size.width/3, 0f), Offset(size.width/3, size.height))
                        drawLine(Color.White.copy(alpha = 0.4f), Offset(size.width*2/3, 0f), Offset(size.width*2/3, size.height))
                    }
                }
            }

            // Card Balance Middle
            Column {
                Text(
                    text = "SALDO DISPONIBLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (hideBalance) "••••••" else {
                        if (wallet.currency == "USD") "$ ${wallet.balance}" else "C$ ${wallet.balance}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Footer Row: Card digits representation & Freeze simulated check button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "•••• •••• •••• 4251",
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 12.sp
                )

                // Freeze Card Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { onFreezeChanged(!frozen) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (frozen) Icons.Default.LockOpen else Icons.Default.AcUnit,
                        contentDescription = "Congelar",
                        tint = if (frozen) Color.Red else Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (frozen) "Descongelar" else "Congelar",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    tx: Transaction,
    hideBalance: Boolean,
    onDelete: () -> Unit
) {
    val theme = LocalPremiumTheme.current
    val isExpense = tx.type == "GASTO"

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
            // Bubble icon matching tag / type
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isExpense) Color.Red.copy(alpha = 0.1f) else theme.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpense) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isExpense) Color.Red else theme.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.note.ifBlank { tx.category },
                    fontWeight = FontWeight.Bold,
                    color = theme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tx.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.secondary
                )
            }

            // Price/Amount
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (hideBalance) "•••" else {
                        val sign = if (isExpense) "-" else "+"
                        val code = tx.currency
                        "$sign %.2f %s".format(tx.amount, code)
                    },
                    fontWeight = FontWeight.Black,
                    color = if (isExpense) Color.Red else theme.accent,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Delete movement shortcut
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar",
                    tint = theme.secondary.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}
