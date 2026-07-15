package com.pancho.suscripciones.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val mono: String,
)

enum class Currency { CLP, USD }
enum class BillingCycle { MENSUAL, ANUAL }

@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val price: Double,
    val currency: Currency,
    val cycle: BillingCycle,
    val nextChargeDateEpochDay: Long,
    val paymentMethod: String,
    val website: String,
    val username: String,
    val passwordEncrypted: String,
    val logoColorHex: String,
    val logoLetter: String,
    val logoImagePath: String? = null,
)
