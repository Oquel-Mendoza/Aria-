package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

sealed interface AuthState {
    object Onboarding : AuthState
    object Locked : AuthState
    object Authenticated : AuthState
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    // Base UI states
    val userProfile: StateFlow<UserProfile?>
    val wallets: StateFlow<List<Wallet>>
    val transactions: StateFlow<List<Transaction>>
    val categories: StateFlow<List<Category>>
    val goals: StateFlow<List<Goal>>
    val notifications: StateFlow<List<NotificationMsg>>

    // Interactive UI Filters for Transactions
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedWalletFilter = MutableStateFlow<Long?>(null)
    val selectedTypeFilter = MutableStateFlow<String?>(null) // "INGRESO", "GASTO", "TRANSFERENCIA"

    // Current app state
    val authState = MutableStateFlow<AuthState>(AuthState.Onboarding)
    val enteredPin = MutableStateFlow("")
    val pinError = MutableStateFlow(false)
    val activeNavScreen = MutableStateFlow("inicio") // "inicio", "movimientos", "estadisticas", "ahorros", "tarjetas", "calendario", "perfil", "notificaciones", "configuracion"

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database)

        userProfile = repository.userProfileFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        wallets = repository.allWalletsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        transactions = repository.allTransactionsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        categories = repository.allCategoriesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        goals = repository.allGoalsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        notifications = repository.allNotificationsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Inicializar categorías y verificar estado de autenticación
        viewModelScope.launch {
            repository.initializeDefaultCategories()
            checkAuthState()
            generateSmartNotifications()
        }
    }

    private suspend fun checkAuthState() {
        val profile = repository.getUserProfile()
        if (profile == null || !profile.onboardingCompleted) {
            authState.value = AuthState.Onboarding
        } else {
            // Si tiene pin o contraseña, arranca bloqueado
            if (profile.pin.isNotEmpty()) {
                authState.value = AuthState.Locked
            } else {
                authState.value = AuthState.Authenticated
            }
        }
    }

    // AUTH ACTIONS
    fun completeOnboarding(username: String, pin: String, initialBalance: Double, currency: String, activeThemeId: String) {
        viewModelScope.launch {
            val hasPin = pin.isNotEmpty()
            val profile = UserProfile(
                username = username,
                pin = pin,
                preferredCurrency = currency,
                activeThemeId = activeThemeId,
                initialBalance = initialBalance,
                onboardingCompleted = true
            )
            repository.saveUserProfile(profile)

            // Crear billetera inicial por defecto
            val walletName = if (currency == "USD") "Cuenta Principal (USD)" else "Cuenta Principal (NIO)"
            val walletId = repository.insertWallet(
                Wallet(
                    name = walletName,
                    type = "BANCO",
                    iconName = "account_balance",
                    colorHex = "#3B82F6",
                    balance = initialBalance,
                    currency = currency
                )
            )

            // Si hay un saldo inicial positivo, crear registro de ingreso
            if (initialBalance > 0.0) {
                // Insertamos la transacción directamente (sin duplicar balance, ya que la billetera ya fue creada con el saldo)
                repository.insertWallet(Wallet(
                    name = "Efectivo",
                    type = "EFECTIVO",
                    iconName = "payments",
                    colorHex = "#10B981",
                    balance = 0.0,
                    currency = currency
                ))
            }

            authState.value = AuthState.Authenticated
            repository.addNotification(
                "¡Bienvenido a Aria! ✨",
                "Te damos la bienvenida al sistema Aria. Tu billetera inicial ha sido creada de forma segura.",
                "INFO"
            )
        }
    }

    fun loginWithPin(pin: String): Boolean {
        var success = false
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            if (profile != null && profile.pin == pin) {
                authState.value = AuthState.Authenticated
                success = true
                pinError.value = false
                enteredPin.value = ""
                generateSmartNotifications() // Generar alertas frescas hoy
            } else {
                pinError.value = true
                enteredPin.value = ""
            }
        }
        return success
    }

    fun logout() {
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            if (profile != null) {
                authState.value = AuthState.Locked
                enteredPin.value = ""
                pinError.value = false
            }
        }
    }

    // FINANCIAL OPERATIONS
    fun createWallet(name: String, type: String, icon: String, color: String, balance: Double, currency: String) {
        viewModelScope.launch {
            repository.insertWallet(
                Wallet(
                    name = name,
                    type = type,
                    iconName = icon,
                    colorHex = color,
                    balance = balance,
                    currency = currency
                )
            )
            repository.addNotification(
                "Nueva Cartera Creada 💼",
                "La cuenta '$name' en $currency fue agregada con un saldo de $balance.",
                "INFO"
            )
        }
    }

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            repository.deleteWallet(wallet)
            repository.addNotification(
                "Cartera Eliminada 🗑️",
                "Se eliminó la cuenta '${wallet.name}' de forma permanente.",
                "ALERTA"
            )
        }
    }

    fun createTransaction(type: String, amount: Double, currency: String, walletId: Long, destWalletId: Long?, category: String, note: String, tags: String, isRec: Boolean, recType: String) {
        viewModelScope.launch {
            val tx = Transaction(
                type = type,
                amount = amount,
                currency = currency,
                walletId = walletId,
                destinationWalletId = destWalletId,
                category = category,
                note = note,
                date = System.currentTimeMillis(),
                tagStr = tags,
                isRecurring = isRec,
                recurrenceType = recType
            )
            repository.addTransaction(tx)
            
            // Trigger smart checking of spend limits
            checkSpendingLimits(amount, type)
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    // CUSTOM CATEGORY
    fun addCustomCategory(name: String, icon: String, color: String, isIncome: Boolean) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(
                    name = name,
                    iconName = icon,
                    colorHex = color,
                    isCustom = true,
                    isIncome = isIncome
                )
            )
        }
    }

    fun deleteCategory(cat: Category) {
        viewModelScope.launch {
            repository.deleteCategory(cat)
        }
    }

    // SAVINGS GOALS
    fun createGoal(name: String, target: Double, date: Long, walletId: Long, icon: String, color: String) {
        viewModelScope.launch {
            repository.insertGoal(
                Goal(
                    name = name,
                    targetAmount = target,
                    targetDate = date,
                    currentAmount = 0.0,
                    walletId = walletId,
                    iconName = icon,
                    colorHex = color,
                    isCompleted = false
                )
            )
            repository.addNotification(
                "Meta de Ahorro Establecida 🎯",
                "Has iniciado tu meta '$name' para ahorrar ${String.format("%.2f", target)} USD.",
                "INFO"
            )
        }
    }

    fun contributeGoal(goal: Goal, amount: Double, fromWalletId: Long) {
        viewModelScope.launch {
            repository.contributeToGoal(goal, amount, fromWalletId)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // SETTINGS & PROFILE
    fun updateTheme(themeId: String) {
        viewModelScope.launch {
            repository.updateTheme(themeId)
        }
    }

    fun updateExchangeRate(rate: Double) {
        viewModelScope.launch {
            repository.updateExchangeRate(rate)
        }
    }

    fun toggleHideBalances() {
        viewModelScope.launch {
            val current = userProfile.value?.hideBalances ?: false
            repository.updateHideBalances(!current)
        }
    }

    fun changePinCode(newPin: String) {
        viewModelScope.launch {
            val current = userProfile.value
            if (current != null) {
                repository.saveUserProfile(current.copy(pin = newPin))
                repository.addNotification(
                    "PIN de Seguridad Actualizado 🔒",
                    "Tu código PIN para bloquear y asegurar Aria fue cambiado exitosamente.",
                    "INFO"
                )
            }
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // EXPORT PROCEDURES
    fun getBackupString(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataAsJson()
            onResult(json)
        }
    }

    fun restoreBackupString(json: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.restoreDataFromJson(json)
            if (ok) {
                repository.addNotification(
                    "Copia de Seguridad Restaurada ✅",
                    "Tus cuentas, movimientos, metas y configuraciones fueron recargados con éxito desde el archivo de respaldo.",
                    "INFO"
                )
            }
            onCompleted(ok)
        }
    }

    fun simulatePdfReport(onResult: (File) -> Unit) {
        // PDF Simulation inside offline device
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val transactionsList = repository.getAllTransactions()
            val walletsList = repository.getAllWallets()
            
            val reportFile = File(context.cacheDir, "Aria_Reporte_Financiero.pdf")
            reportFile.writeText(buildString {
                append("=========================================\n")
                append("        ARIA FINANCIAL APP REPORT        \n")
                append("=========================================\n")
                append("Generado el: ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())}\n\n")
                append("RESUMEN DE CUENTAS:\n")
                for (w in walletsList) {
                    append("- ${w.name}: ${w.balance} ${w.currency}\n")
                }
                append("\nÚLTIMOS MOVIMIENTOS RECIENTES:\n")
                for (t in transactionsList.take(50)) {
                    append("[${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(t.date))}] ")
                    append("${t.type} - ${t.category}: ${t.amount} ${t.currency} (${t.note})\n")
                }
                append("\n=========================================\n")
                append("Fin de reporte. Aria Digital Banking Offline.\n")
            })
            onResult(reportFile)
        }
    }

    // SMART NOTIFICATION REASONER ENGINE
    private suspend fun checkSpendingLimits(amount: Double, type: String) {
        if (type == "GASTO") {
            if (amount > 150.0) {
                repository.addNotification(
                    "Alerta de Gasto Elevado ⚠️",
                    "Has registrado un pago único de ${String.format("%.2f", amount)} USD/NIO. Cuida tus cuotas mensuales.",
                    "ALERTA"
                )
            }
        }
    }

    private suspend fun generateSmartNotifications() {
        val txs = repository.getAllTransactions()
        val mGoals = repository.getAllGoals()
        
        // 1. Alerta si no se han registrado gastos hoy
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        
        val todayTxCount = txs.count { it.date >= today }
        if (todayTxCount == 0) {
            repository.addNotification(
                "Actualiza tus Registros Diarios 🗒️",
                "No has registrado gastos hoy en Aria. Informar sobre tus gastos pequeños ayuda a erradicar fugas de efectivo.",
                "INFO"
            )
        }

        // 2. Alerta de meta de ahorro casi completa
        for (g in mGoals) {
            if (!g.isCompleted && g.targetAmount > 0) {
                val ratio = g.currentAmount / g.targetAmount
                if (ratio >= 0.85) {
                    repository.addNotification(
                        "¡Casi lo logras! 🎯",
                        "Tu meta de ahorro '${g.name}' está al ${String.format("%.0f", ratio * 100)}% de completarse. ¡Un último esfuerzo!",
                        "INFO"
                    )
                }
            }
        }

        // 3. Alarma mensual de ingresos vs egresos
        var monthlyIncome = 0.0
        var monthlyExpense = 0.0
        val thisMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        for (tx in txs.filter { it.date >= thisMonth }) {
            val value = tx.amount
            if (tx.type == "INGRESO") {
                monthlyIncome += value
            } else if (tx.type == "GASTO") {
                monthlyExpense += value
            }
        }

        if (monthlyExpense > monthlyIncome && monthlyIncome > 0) {
            repository.addNotification(
                "¡Alerta de Presupuesto! 🚨",
                "Tus deudas/egresos de este mes superan tus ingresos totales. Te recomendamos moderar tus compras recreativas.",
                "ALERTA"
            )
        } else if (monthlyIncome > monthlyExpense && monthlyExpense > 0) {
            repository.addNotification(
                "¡Excelente Gestión Financiera! ❇️",
                "Mantienes un margen de ahorro del ${String.format("%.0f", ((monthlyIncome - monthlyExpense) / monthlyIncome) * 100)}% estas semanas.",
                "INFO"
            )
        }
    }
}
