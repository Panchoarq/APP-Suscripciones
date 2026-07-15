package com.pancho.suscripciones.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pancho.suscripciones.data.AppDatabase
import com.pancho.suscripciones.data.CategoryEntity
import com.pancho.suscripciones.data.Currency
import com.pancho.suscripciones.data.PaymentMethodEntity
import com.pancho.suscripciones.data.PreferencesRepository
import com.pancho.suscripciones.data.SubscriptionEntity
import com.pancho.suscripciones.data.SubscriptionsRepository
import com.pancho.suscripciones.data.VisualStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Tasa referencial USD -> CLP. TODO producción: reemplazar por consulta a mindicador.cl (cacheada). */
const val USD_CLP_RATE = 950.0

data class CategoryWithSubs(
    val category: CategoryEntity,
    val subs: List<SubscriptionEntity>,
)

data class HomeUiState(
    val categories: List<CategoryWithSubs> = emptyList(),
    val totalMonthlyClp: Double = 0.0,
    val hasUsdSubscription: Boolean = false,
    val darkTheme: Boolean = true,
    val visualStyle: VisualStyle = VisualStyle.LISTA,
    val paymentMethods: List<PaymentMethodEntity> = emptyList(),
)

fun SubscriptionEntity.monthlyEquivalentClp(): Double {
    val monthly = if (cycle == com.pancho.suscripciones.data.BillingCycle.ANUAL) price / 12 else price
    return if (currency == Currency.USD) monthly * USD_CLP_RATE else monthly
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.get(application)
    val repository = SubscriptionsRepository(db)
    private val prefs = PreferencesRepository(application)

    private val expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    val expandedCategoryIds: StateFlow<Set<Long>> = expandedIds

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeCategories(),
        repository.observeSubscriptions(),
        prefs.isDarkTheme,
        prefs.visualStyle,
        repository.observePaymentMethods(),
    ) { categories, subs, dark, style, paymentMethods ->
        val grouped = categories.map { cat ->
            CategoryWithSubs(
                category = cat,
                subs = subs.filter { it.categoryId == cat.id }
                    .sortedBy { it.nextChargeDateEpochDay },
            )
        }
        HomeUiState(
            categories = grouped,
            totalMonthlyClp = subs.sumOf { it.monthlyEquivalentClp() },
            hasUsdSubscription = subs.any { it.currency == Currency.USD },
            darkTheme = dark,
            visualStyle = style,
            paymentMethods = paymentMethods,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        viewModelScope.launch {
            if (!prefs.hasSeededDefaults.first()) {
                seedDefaultData(repository)
                prefs.setHasSeededDefaults()
            }
        }
    }

    fun toggleExpanded(categoryId: Long) {
        expandedIds.value = expandedIds.value.toMutableSet().apply {
            if (!add(categoryId)) remove(categoryId)
        }
    }

    fun toggleTheme() = viewModelScope.launch {
        prefs.setDarkTheme(!uiState.value.darkTheme)
    }

    fun cycleVisualStyle() = viewModelScope.launch {
        val next = when (uiState.value.visualStyle) {
            VisualStyle.LISTA -> VisualStyle.MAQUINA_ESCRIBIR
            VisualStyle.MAQUINA_ESCRIBIR -> VisualStyle.SUAVE
            VisualStyle.SUAVE -> VisualStyle.LISTA
        }
        prefs.setVisualStyle(next)
    }

    fun saveCategory(category: CategoryEntity) = viewModelScope.launch {
        repository.saveCategory(category)
    }

    fun deleteCategory(category: CategoryEntity) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    fun saveSubscription(sub: SubscriptionEntity, plainPassword: String) = viewModelScope.launch {
        repository.saveSubscription(sub, plainPassword)
    }

    fun deleteSubscription(sub: SubscriptionEntity) = viewModelScope.launch {
        repository.deleteSubscription(sub)
    }

    fun savePaymentMethod(method: PaymentMethodEntity) = viewModelScope.launch {
        repository.savePaymentMethod(method)
    }

    fun deletePaymentMethod(method: PaymentMethodEntity) = viewModelScope.launch {
        repository.deletePaymentMethod(method)
    }
}
