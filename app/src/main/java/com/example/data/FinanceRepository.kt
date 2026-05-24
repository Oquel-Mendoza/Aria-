package com.example.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class FinanceRepository(private val db: AppDatabase) {
    private val userProfileDao = db.userProfileDao()
    private val walletDao = db.walletDao()
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val goalDao = db.goalDao()
    private val notificationDao = db.notificationDao()

    // Flow getters
    val userProfileFlow: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()
    val allWalletsFlow: Flow<List<Wallet>> = walletDao.getAllWalletsFlow()
    val allTransactionsFlow: Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow()
    val allCategoriesFlow: Flow<List<Category>> = categoryDao.getAllCategoriesFlow()
    val allGoalsFlow: Flow<List<Goal>> = goalDao.getAllGoalsFlow()
    val allNotificationsFlow: Flow<List<NotificationMsg>> = notificationDao.getAllNotificationsFlow()

    // Suspend getters
    suspend fun getUserProfile(): UserProfile? = userProfileDao.getUserProfile()
    suspend fun getAllWallets(): List<Wallet> = walletDao.getAllWallets()
    suspend fun getAllTransactions(): List<Transaction> = transactionDao.getAllTransactions()
    suspend fun getAllCategories(): List<Category> = categoryDao.getAllCategories()
    suspend fun getAllGoals(): List<Goal> = goalDao.getAllGoals()

    // Profile & Meta operations
    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun updateHideBalances(hide: Boolean) {
        userProfileDao.updateHideBalances(hide)
    }

    suspend fun updateTheme(themeId: String) {
        userProfileDao.updateTheme(themeId)
    }

    suspend fun updateExchangeRate(rate: Double) {
        userProfileDao.updateExchangeRate(rate)
    }

    // Wallets
    suspend fun insertWallet(wallet: Wallet): Long = walletDao.insertWallet(wallet)
    suspend fun updateWallet(wallet: Wallet) = walletDao.updateWallet(wallet)
    suspend fun deleteWallet(wallet: Wallet) = walletDao.deleteWallet(wallet)

    // Transactions with Automatic Balance Adjustment
    suspend fun addTransaction(tx: Transaction): Long {
        var txId: Long = 0L
        db.withTransaction {
            // Guardar transacción
            txId = db.transactionDao().insertTransaction(tx)
            
            // Ajustar balance de la billetera origen
            val sourceWallet = db.walletDao().getWalletById(tx.walletId)
            if (sourceWallet != null) {
                val newBalance = when (tx.type) {
                    "GASTO" -> sourceWallet.balance - tx.amount
                    "INGRESO" -> sourceWallet.balance + tx.amount
                    "TRANSFERENCIA" -> sourceWallet.balance - tx.amount
                    else -> sourceWallet.balance
                }
                db.walletDao().updateBalance(tx.walletId, newBalance)
            }

            // Si es transferencia, ajustar billetera destino
            if (tx.type == "TRANSFERENCIA" && tx.destinationWalletId != null) {
                val destWallet = db.walletDao().getWalletById(tx.destinationWalletId)
                if (destWallet != null) {
                    // Si las monedas difieren, convertir monto.
                    // Para simplificar, asumimos que el monto se especifica en la moneda de la billetera origen (tx.currency)
                    val amountToAdd = if (sourceWallet?.currency == destWallet.currency) {
                        tx.amount
                    } else {
                        // Monedas difieren
                        val profile = db.userProfileDao().getUserProfile()
                        val rate = profile?.usdToNioRate ?: 36.5
                        if (tx.currency == "USD" && destWallet.currency == "NIO") {
                            tx.amount * rate
                        } else if (tx.currency == "NIO" && destWallet.currency == "USD") {
                            tx.amount / rate
                        } else {
                            tx.amount
                        }
                    }
                    val newDestBalance = destWallet.balance + amountToAdd
                    db.walletDao().updateBalance(tx.destinationWalletId, newDestBalance)
                }
            }
        }
        return txId
    }

    suspend fun deleteTransaction(tx: Transaction) {
        db.withTransaction {
            db.transactionDao().deleteTransaction(tx)

            // Revertir balance billetera origen
            val sourceWallet = db.walletDao().getWalletById(tx.walletId)
            if (sourceWallet != null) {
                val revertedBalance = when (tx.type) {
                    "GASTO" -> sourceWallet.balance + tx.amount
                    "INGRESO" -> sourceWallet.balance - tx.amount
                    "TRANSFERENCIA" -> sourceWallet.balance + tx.amount
                    else -> sourceWallet.balance
                }
                db.walletDao().updateBalance(tx.walletId, revertedBalance)
            }

            // Revertir si es transferencia
            if (tx.type == "TRANSFERENCIA" && tx.destinationWalletId != null) {
                val destWallet = db.walletDao().getWalletById(tx.destinationWalletId)
                if (destWallet != null) {
                    val amountToRemove = if (sourceWallet?.currency == destWallet.currency) {
                        tx.amount
                    } else {
                        val profile = db.userProfileDao().getUserProfile()
                        val rate = profile?.usdToNioRate ?: 36.5
                        if (tx.currency == "USD" && destWallet.currency == "NIO") {
                            tx.amount * rate
                        } else if (tx.currency == "NIO" && destWallet.currency == "USD") {
                            tx.amount / rate
                        } else {
                            tx.amount
                        }
                    }
                    val revertedDestBalance = destWallet.balance - amountToRemove
                    db.walletDao().updateBalance(tx.destinationWalletId, revertedDestBalance)
                }
            }
        }
    }

    // Categories
    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    // Goals & contributions
    suspend fun insertGoal(goal: Goal): Long = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)

    // Contribución a meta de ahorro
    suspend fun contributeToGoal(goal: Goal, amount: Double, fromWalletId: Long) {
        db.withTransaction {
            val wallet = db.walletDao().getWalletById(fromWalletId)
            if (wallet != null && wallet.balance >= amount) {
                // Descontar de la billetera
                db.walletDao().updateBalance(fromWalletId, wallet.balance - amount)
                
                // Con esto creamos gastos ficticios de meta de ahorro, o simplemente agregamos un registro de tipo transferencia/gasto a meta de ahorro
                val tx = Transaction(
                    type = "GASTO",
                    amount = amount,
                    currency = wallet.currency,
                    walletId = fromWalletId,
                    category = "Ahorros",
                    note = "Aporte a meta: ${goal.name}",
                    date = System.currentTimeMillis()
                )
                db.transactionDao().insertTransaction(tx)

                // Actualizar meta
                // Si la moneda difiere, convertimos
                val amountForGoal = if (wallet.currency == "USD") {
                    // Las metas las guardaremos por defecto en la moneda seleccionada, o asumimos moneda compatible.
                    // Para simplificar, la meta conserva su saldo independiente incrementado
                    amount
                } else {
                    val profile = db.userProfileDao().getUserProfile()
                    val rate = profile?.usdToNioRate ?: 36.5
                    amount / rate // Pasar de NIO a base USD para la meta
                }

                val newAmount = goal.currentAmount + amountForGoal
                val isCompleted = newAmount >= goal.targetAmount
                db.goalDao().updateGoal(goal.copy(currentAmount = newAmount, isCompleted = isCompleted))
                
                // Si la completó, generamos una notificación premiun
                if (isCompleted) {
                    db.notificationDao().insertNotification(
                        NotificationMsg(
                            title = "¡Meta de Ahorro Alcanzada! 🏆",
                            message = "Has completado tu meta '${goal.name}' de ${String.format("%.2f", goal.targetAmount)} USD. ¡Excelente disciplina financiera!",
                            date = System.currentTimeMillis(),
                            typeString = "META"
                        )
                    )
                }
            }
        }
    }

    // Notifications
    suspend fun addNotification(title: String, message: String, type: String = "INFO") {
        notificationDao.insertNotification(
            NotificationMsg(
                title = title,
                message = message,
                date = System.currentTimeMillis(),
                typeString = type
            )
        )
    }

    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()
    suspend fun deleteNotification(id: Long) = notificationDao.deleteNotification(id)
    suspend fun clearAllNotifications() = notificationDao.clearAllNotifications()

    // EXPORT / IMPORT (Backup as JSON)
    suspend fun exportDataAsJson(): String {
        val user = userProfileDao.getUserProfile() ?: UserProfile()
        val wallets = walletDao.getAllWallets()
        val txs = transactionDao.getAllTransactions()
        val cats = categoryDao.getAllCategories()
        val goals = goalDao.getAllGoals()

        val backupObj = JSONObject()
        
        // User
        val userObj = JSONObject().apply {
            put("username", user.username)
            put("preferredCurrency", user.preferredCurrency)
            put("activeThemeId", user.activeThemeId)
            put("usdToNioRate", user.usdToNioRate)
        }
        backupObj.put("user_profile", userObj)

        // Wallets
        val walletsArr = JSONArray()
        for (w in wallets) {
            walletsArr.put(JSONObject().apply {
                put("name", w.name)
                put("type", w.type)
                put("iconName", w.iconName)
                put("colorHex", w.colorHex)
                put("balance", w.balance)
                put("currency", w.currency)
            })
        }
        backupObj.put("wallets", walletsArr)

        // Transactions
        val txsArr = JSONArray()
        for (t in txs) {
            txsArr.put(JSONObject().apply {
                put("type", t.type)
                put("amount", t.amount)
                put("currency", t.currency)
                put("category", t.category)
                put("note", t.note)
                put("date", t.date)
                put("tagStr", t.tagStr)
                put("isRecurring", t.isRecurring)
                put("recurrenceType", t.recurrenceType)
            })
        }
        backupObj.put("transactions", txsArr)

        // Categories
        val catsArr = JSONArray()
        for (c in cats) {
            catsArr.put(JSONObject().apply {
                put("name", c.name)
                put("iconName", c.iconName)
                put("colorHex", c.colorHex)
                put("isCustom", c.isCustom)
                put("isIncome", c.isIncome)
            })
        }
        backupObj.put("categories", catsArr)

        // Goals
        val goalsArr = JSONArray()
        for (g in goals) {
            goalsArr.put(JSONObject().apply {
                put("name", g.name)
                put("targetAmount", g.targetAmount)
                put("targetDate", g.targetDate)
                put("currentAmount", g.currentAmount)
                put("iconName", g.iconName)
                put("colorHex", g.colorHex)
                put("isCompleted", g.isCompleted)
            })
        }
        backupObj.put("goals", goalsArr)

        return backupObj.toString(2)
    }

    suspend fun restoreDataFromJson(jsonStr: String): Boolean {
        return try {
            val backup = JSONObject(jsonStr)
            db.withTransaction {
                // Perfil
                if (backup.has("user_profile")) {
                    val pObj = backup.getJSONObject("user_profile")
                    val existing = userProfileDao.getUserProfile() ?: UserProfile()
                    userProfileDao.insertOrUpdateProfile(existing.copy(
                        username = pObj.optString("username", existing.username),
                        preferredCurrency = pObj.optString("preferredCurrency", existing.preferredCurrency),
                        activeThemeId = pObj.optString("activeThemeId", existing.activeThemeId),
                        usdToNioRate = pObj.optDouble("usdToNioRate", existing.usdToNioRate)
                    ))
                }

                // Wallets
                if (backup.has("wallets")) {
                    // Borrar existentes
                    val existingW = walletDao.getAllWallets()
                    for (w in existingW) walletDao.deleteWallet(w)
                    
                    val wArr = backup.getJSONArray("wallets")
                    for (i in 0 until wArr.length()) {
                        val wObj = wArr.getJSONObject(i)
                        walletDao.insertWallet(Wallet(
                            name = wObj.getString("name"),
                            type = wObj.getString("type"),
                            iconName = wObj.getString("iconName"),
                            colorHex = wObj.getString("colorHex"),
                            balance = wObj.getDouble("balance"),
                            currency = wObj.getString("currency")
                        ))
                    }
                }

                // Categorias
                if (backup.has("categories")) {
                    val existingC = categoryDao.getAllCategories()
                    for (c in existingC) {
                        if (c.isCustom) categoryDao.deleteCategory(c)
                    }
                    val cArr = backup.getJSONArray("categories")
                    for (i in 0 until cArr.length()) {
                        val cObj = cArr.getJSONObject(i)
                        // Sólo insertar si es personalizada o no existía
                        categoryDao.insertCategory(Category(
                            name = cObj.getString("name"),
                            iconName = cObj.getString("iconName"),
                            colorHex = cObj.getString("colorHex"),
                            isCustom = cObj.optBoolean("isCustom", false),
                            isIncome = cObj.optBoolean("isIncome", false)
                        ))
                    }
                }

                // Transacciones
                if (backup.has("transactions")) {
                    val existingT = transactionDao.getAllTransactions()
                    for (t in existingT) transactionDao.deleteTransaction(t)

                    // Obtenemos wallets recién insertadas para asociar IDs coherentes
                    val newWallets = walletDao.getAllWallets()
                    val firstWalletId = newWallets.firstOrNull()?.id ?: 1L
                    
                    val tArr = backup.getJSONArray("transactions")
                    for (i in 0 until tArr.length()) {
                        val tObj = tArr.getJSONObject(i)
                        transactionDao.insertTransaction(Transaction(
                            type = tObj.getString("type"),
                            amount = tObj.getDouble("amount"),
                            currency = tObj.getString("currency"),
                            walletId = firstWalletId, // Vinculamos a la primera billetera por defecto en restauración
                            category = tObj.getString("category"),
                            note = tObj.optString("note", ""),
                            date = tObj.getLong("date"),
                            tagStr = tObj.optString("tagStr", ""),
                            isRecurring = tObj.optBoolean("isRecurring", false),
                            recurrenceType = tObj.optString("recurrenceType", "NINGUNO")
                        ))
                    }
                }

                // Metas
                if (backup.has("goals")) {
                    val existingG = goalDao.getAllGoals()
                    for (g in existingG) goalDao.deleteGoal(g)

                    val newWallets = walletDao.getAllWallets()
                    val firstWalletId = newWallets.firstOrNull()?.id ?: 1L

                    val gArr = backup.getJSONArray("goals")
                    for (i in 0 until gArr.length()) {
                        val gObj = gArr.getJSONObject(i)
                        goalDao.insertGoal(Goal(
                            name = gObj.getString("name"),
                            targetAmount = gObj.getDouble("targetAmount"),
                            targetDate = gObj.getLong("targetDate"),
                            currentAmount = gObj.getDouble("currentAmount"),
                            walletId = firstWalletId,
                            iconName = gObj.getString("iconName"),
                            colorHex = gObj.getString("colorHex"),
                            isCompleted = gObj.optBoolean("isCompleted", false)
                        ))
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Inicialización de categorías base si están vacías
    suspend fun initializeDefaultCategories() {
        val count = categoryDao.getAllCategories().size
        if (count == 0) {
            val defaults = listOf(
                Category(name = "Comida", iconName = "restaurant", colorHex = "#FF5722", isCustom = false, isIncome = false),
                Category(name = "Transporte", iconName = "directions_car", colorHex = "#2196F3", isCustom = false, isIncome = false),
                Category(name = "Compras", iconName = "shopping_bag", colorHex = "#9C27B0", isCustom = false, isIncome = false),
                Category(name = "Entretenimiento", iconName = "sports_esports", colorHex = "#E91E63", isCustom = false, isIncome = false),
                Category(name = "Facturas", iconName = "receipt", colorHex = "#F44336", isCustom = false, isIncome = false),
                Category(name = "Educación", iconName = "school", colorHex = "#3F51B5", isCustom = false, isIncome = false),
                Category(name = "Salud", iconName = "favorite", colorHex = "#4CAF50", isCustom = false, isIncome = false),
                Category(name = "Ahorros", iconName = "savings", colorHex = "#009688", isCustom = false, isIncome = false),
                Category(name = "Inversiones", iconName = "trending_up", colorHex = "#00BCD4", isCustom = false, isIncome = false),
                Category(name = "Salario", iconName = "payments", colorHex = "#4CAF50", isCustom = false, isIncome = true),
                Category(name = "Depósitos", iconName = "account_balance_wallet", colorHex = "#8BC34A", isCustom = false, isIncome = true),
                Category(name = "Otros", iconName = "category", colorHex = "#607D8B", isCustom = false, isIncome = false)
            )
            for (c in defaults) {
                categoryDao.insertCategory(c)
            }
        }
    }
}
