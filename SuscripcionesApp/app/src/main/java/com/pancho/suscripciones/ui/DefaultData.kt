package com.pancho.suscripciones.ui

import com.pancho.suscripciones.data.BillingCycle
import com.pancho.suscripciones.data.CategoryEntity
import com.pancho.suscripciones.data.Currency
import com.pancho.suscripciones.data.PaymentMethodEntity
import com.pancho.suscripciones.data.SubscriptionEntity
import com.pancho.suscripciones.data.SubscriptionsRepository
import java.time.LocalDate

private data class DefaultSub(val name: String, val categoryName: String)

private val defaultCategories = listOf(
    "Cloud" to "#5B8CFF",
    "Compras" to "#F59E0B",
    "Delivery" to "#EF4444",
    "Streaming" to "#A855F7",
    "Supermercado" to "#22C55E",
    "Transporte" to "#06B6D4",
)

private val defaultSubscriptions = listOf(
    DefaultSub("OneDrive", "Cloud"),
    DefaultSub("Google Drive", "Cloud"),
    DefaultSub("Mercado Libre", "Compras"),
    DefaultSub("Rappi Pro", "Delivery"),
    DefaultSub("Uber Eats Pro", "Delivery"),
    DefaultSub("Netflix", "Streaming"),
    DefaultSub("Amazon Prime", "Streaming"),
    DefaultSub("HBO Max", "Streaming"),
    DefaultSub("Disney+", "Streaming"),
    DefaultSub("Paramount+", "Streaming"),
    DefaultSub("Apple TV+", "Streaming"),
    DefaultSub("Spotify", "Streaming"),
    DefaultSub("Jumbo", "Supermercado"),
    DefaultSub("Uber", "Transporte"),
    DefaultSub("Cabify", "Transporte"),
)

private val defaultPaymentMethods = listOf("Tarjeta de crédito", "Tarjeta de débito", "Transferencia")

/** Precarga categorías, suscripciones y medios de pago de ejemplo. Se corre una sola vez (ver hasSeededDefaults). */
suspend fun seedDefaultData(repository: SubscriptionsRepository) {
    val categoryIds = mutableMapOf<String, Long>()
    defaultCategories.forEach { (name, colorHex) ->
        val id = repository.saveCategory(CategoryEntity(name = name, colorHex = colorHex, mono = monoForName(name)))
        categoryIds[name] = id
    }

    defaultSubscriptions.forEach { def ->
        val categoryId = categoryIds[def.categoryName] ?: return@forEach
        val brand = findBrand(def.name)
        val entity = SubscriptionEntity(
            categoryId = categoryId,
            name = def.name,
            price = 0.0,
            currency = Currency.CLP,
            cycle = BillingCycle.MENSUAL,
            nextChargeDateEpochDay = LocalDate.now().plusMonths(1).toEpochDay(),
            paymentMethod = "",
            website = brand?.domain ?: "",
            username = "",
            passwordEncrypted = "",
            logoColorHex = brand?.colorHex ?: "#5B8CFF",
            logoLetter = monoForName(def.name).take(1),
        )
        repository.saveSubscription(entity, "")
    }

    defaultPaymentMethods.forEach { name ->
        repository.savePaymentMethod(PaymentMethodEntity(name = name))
    }
}
