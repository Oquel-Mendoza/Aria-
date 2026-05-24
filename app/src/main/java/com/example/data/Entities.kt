package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "",
    val passwordHash: String = "",
    val pin: String = "",
    val preferredCurrency: String = "USD", // "USD" o "NIO"
    val activeThemeId: String = "MINIMAL_WHITE",
    val initialBalance: Double = 0.0,
    val onboardingCompleted: Boolean = false,
    val profileImageUri: String? = null,
    val hideBalances: Boolean = false,
    val usdToNioRate: Double = 36.5 // Tasa de cambio por defecto
)

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "EFECTIVO", "BANCO", "AHORROS", "EMERGENCIA", "REAL"
    val iconName: String,
    val colorHex: String,
    val balance: Double,
    val currency: String // "USD" o "NIO"
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INGRESO", "GASTO", "TRANSFERENCIA"
    val amount: Double,
    val currency: String, // "USD" o "NIO"
    val walletId: Long, // ID de billetera origen
    val destinationWalletId: Long? = null, // ID de billetera destino (para transferencias)
    val category: String,
    val note: String,
    val date: Long, // Epoch timestamp (ms)
    val tagStr: String = "", // Etiquetas separadas por comas
    val isRecurring: Boolean = false,
    val recurrenceType: String = "NINGUNO" // "NINGUNO", "DIARIO", "SEMANAL", "MENSUAL"
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val isCustom: Boolean = false,
    val isIncome: Boolean = false
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val targetDate: Long, // Epoch timestamp (ms)
    val currentAmount: Double,
    val walletId: Long, // Billetera vinculada
    val iconName: String,
    val colorHex: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationMsg(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val date: Long, // Epoch timestamp (ms)
    val read: Boolean = false,
    val typeString: String = "INFO" // "INFO", "ALERTA", "EXCESO", "META"
)
