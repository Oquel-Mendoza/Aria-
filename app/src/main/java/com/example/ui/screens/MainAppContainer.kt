package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.LocalPremiumTheme
import com.example.viewmodel.FinanceViewModel

@Composable
fun MainAppContainer(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalPremiumTheme.current
    val userProfile by viewModel.userProfile.collectAsState()
    val activeScreen by viewModel.activeNavScreen.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val username = userProfile?.username ?: "Usuario"

    // Overlay controllers
    var showProfileOverlay by remember { mutableStateOf(false) }
    var showCalendarOverlay by remember { mutableStateOf(false) }
    var showNotificationsOverlay by remember { mutableStateOf(false) }

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.read }
    }

    Box(modifier = modifier.fillMaxSize().background(theme.background)) {
        Scaffold(
            topBar = {
                OptInTopBar(
                    username = username,
                    notificationsCount = unreadNotificationsCount,
                    onProfileClick = { showProfileOverlay = true },
                    onCalendarClick = { showCalendarOverlay = true },
                    onNotificationsClick = { showNotificationsOverlay = true }
                )
            },
            bottomBar = {
                CustomBottomNavigationBar(
                    activeScreen = activeScreen,
                    onTabSelect = { viewModel.activeNavScreen.value = it }
                )
            },
            containerColor = theme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(theme.background)
            ) {
                // Dynamic Page switcher
                AnimatedContent(
                    targetState = activeScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) togetherWith 
                        fadeOut(animationSpec = androidx.compose.animation.core.tween(250))
                    },
                    modifier = Modifier.fillMaxSize()
                ) { screenId ->
                    when (screenId) {
                        "inicio" -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToTransactions = { viewModel.activeNavScreen.value = "movimientos" },
                            modifier = Modifier.fillMaxSize()
                        )
                        "movimientos" -> TransactionsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        "estadisticas" -> StatisticsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        "ahorros" -> SavingsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        "configuracion" -> SettingsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // FULL SHEET SCREEN OVERLAY PANELS
        AnimatedVisibility(
            visible = showProfileOverlay,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ProfileScreen(
                viewModel = viewModel,
                onClose = { showProfileOverlay = false },
                modifier = Modifier.fillMaxSize().testTag("profile_overlay_modal")
            )
        }

        AnimatedVisibility(
            visible = showCalendarOverlay,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            CalendarScreen(
                viewModel = viewModel,
                onClose = { showCalendarOverlay = false },
                modifier = Modifier.fillMaxSize().testTag("calendar_overlay_modal")
            )
        }

        AnimatedVisibility(
            visible = showNotificationsOverlay,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NotificationsScreen(
                viewModel = viewModel,
                onClose = { showNotificationsOverlay = false },
                modifier = Modifier.fillMaxSize().testTag("notifications_overlay_modal")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptInTopBar(
    username: String,
    notificationsCount: Int,
    onProfileClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val theme = LocalPremiumTheme.current
    TopAppBar(
        title = {
            Text(
                text = "ARIA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = theme.primary
            )
        },
        navigationIcon = {
            // Left Profile Avatar
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, end = 8.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(theme.primary.copy(alpha = 0.15f))
                    .border(1.dp, theme.primary, CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary
                )
            }
        },
        actions = {
            // Calendar grid shortcut
            IconButton(onClick = onCalendarClick) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendario",
                    tint = theme.primary
                )
            }

            // Bell notifications icon
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (notificationsCount > 0) {
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text("$notificationsCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = theme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = theme.background,
            titleContentColor = theme.primary
        )
    )
}

@Composable
fun CustomBottomNavigationBar(
    activeScreen: String,
    onTabSelect: (String) -> Unit
) {
    val theme = LocalPremiumTheme.current
    val navItems = listOf(
        Triple("Inicio", Icons.Default.Home, "inicio"),
        Triple("Movimientos", Icons.Default.SwapHoriz, "movimientos"),
        Triple("Estadísticas", Icons.Default.BarChart, "estadisticas"),
        Triple("Ahorros", Icons.Default.Savings, "ahorros"),
        Triple("Ajustes", Icons.Default.Settings, "configuracion")
    )

    NavigationBar(
        containerColor = theme.surface,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(64.dp)
            .testTag("app_bottom_bar")
    ) {
        navItems.forEach { (label, icon, route) ->
            val isSelected = activeScreen == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) theme.primary else theme.secondary
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) theme.primary else theme.secondary,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = theme.primary.copy(alpha = 0.12f)
                ),
                modifier = Modifier.testTag("nav_tab_$route")
            )
        }
    }
}
