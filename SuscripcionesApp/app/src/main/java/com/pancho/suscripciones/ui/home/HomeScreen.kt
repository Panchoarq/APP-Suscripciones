package com.pancho.suscripciones.ui.home

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pancho.suscripciones.data.CategoryEntity
import com.pancho.suscripciones.data.SubscriptionEntity
import com.pancho.suscripciones.ui.AppViewModel
import com.pancho.suscripciones.ui.CategoryWithSubs
import com.pancho.suscripciones.ui.iconForCategory
import com.pancho.suscripciones.ui.theme.LocalExtraColors
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onAddCategory: () -> Unit,
    onAddSubscription: (categoryId: Long) -> Unit,
    onEditCategory: (Long) -> Unit,
    onOpenSubscription: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val expanded by viewModel.expandedCategoryIds.collectAsState()
    val extra = LocalExtraColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Suscripciones", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CircleIconButton(size = 32.dp, onClick = { viewModel.cycleVisualStyle() }) {
                    Icon(Icons.Filled.Autorenew, contentDescription = "Cambiar estilo visual", tint = MaterialTheme.colorScheme.onSurface)
                }
                CircleIconButton(size = 40.dp, onClick = { onAddCategory() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = MaterialTheme.colorScheme.onSurface)
                }
                CircleIconButton(size = 40.dp, onClick = { viewModel.toggleTheme() }) {
                    Icon(
                        if (state.darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Cambiar tema",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { SummaryCard(state.totalMonthlyClp, state.hasUsdSubscription, state.categories.sumOf { it.subs.size }) }

            items(state.categories, key = { it.category.id }) { catWithSubs ->
                CategoryRow(
                    data = catWithSubs,
                    expanded = expanded.contains(catWithSubs.category.id),
                    onToggle = { viewModel.toggleExpanded(catWithSubs.category.id) },
                    onEdit = { onEditCategory(catWithSubs.category.id) },
                    onAddSub = { onAddSubscription(catWithSubs.category.id) },
                    onOpenSub = onOpenSubscription,
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .clickable { onAddCategory() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+ Nueva categoría", color = extra.textMuted)
                }
            }
        }
    }
}

@Composable
private fun CircleIconButton(size: androidx.compose.ui.unit.Dp, onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
    ) { content() }
}

private val clpFormatter = NumberFormat.getNumberInstance(Locale("es", "CL"))

@Composable
private fun SummaryCard(totalMonthlyClp: Double, hasUsd: Boolean, activeCount: Int) {
    val extra = LocalExtraColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Text(
            "GASTO MENSUAL TOTAL",
            style = MaterialTheme.typography.labelSmall,
            color = extra.textMuted,
        )
        Text(
            "$${clpFormatter.format(totalMonthlyClp.toLong())}",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "$${clpFormatter.format((totalMonthlyClp * 12).toLong())} al año",
                style = MaterialTheme.typography.labelMedium,
                color = extra.textMuted,
            )
            Text(
                "$activeCount suscripciones activas",
                style = MaterialTheme.typography.labelMedium,
                color = extra.textMuted,
            )
        }
        if (hasUsd) {
            Text(
                "Convierte USD a CLP a $950 por dólar (referencial, mindicador.cl)",
                style = MaterialTheme.typography.labelMedium,
                color = extra.textMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun CategoryRow(
    data: CategoryWithSubs,
    expanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onAddSub: () -> Unit,
    onOpenSub: (Long) -> Unit,
) {
    val extra = LocalExtraColors.current
    val category = data.category
    val color = Color(android.graphics.Color.parseColor(category.colorHex))
    val subtotal = data.subs.sumOf { it.monthlyEquivalentClpSafe() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                val icon = iconForCategory(category.name)
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                } else {
                    Text(category.mono, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.bodyMedium, color = extra.textMuted)
                Text(
                    "$${clpFormatter.format(subtotal.toLong())}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar categoría", tint = MaterialTheme.colorScheme.onSurface)
            }
            Box(
                modifier = Modifier
                    .background(extra.surface2, RoundedCornerShape(100.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("${data.subs.size} SUSCRIP.", style = MaterialTheme.typography.labelSmall, color = extra.textMuted)
            }
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.rotate(if (expanded) 180f else 0f),
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                data.subs.forEach { sub ->
                    SubscriptionRow(sub, onClick = { onOpenSub(sub.id) })
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddSub() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+ Agregar suscripción", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun SubscriptionRow(sub: SubscriptionEntity, onClick: () -> Unit) {
    val extra = LocalExtraColors.current
    val color = Color(android.graphics.Color.parseColor(sub.logoColorHex))
    val date = LocalDate.ofEpochDay(sub.nextChargeDateEpochDay)
    val dateStr = date.format(DateTimeFormatter.ofPattern("d MMM", Locale("es")))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(0.dp, MaterialTheme.colorScheme.outline)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val logoPath = sub.logoImagePath
        val logoBitmap = remember(logoPath) { logoPath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() } }
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(color, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (logoBitmap != null) {
                Image(bitmap = logoBitmap, contentDescription = null, modifier = Modifier.size(22.dp))
            } else {
                Text(sub.logoLetter, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(sub.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("Próx. cobro · $dateStr", style = MaterialTheme.typography.labelMedium, color = extra.textMuted)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$${clpFormatter.format(sub.price.toLong())}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                if (sub.cycle == com.pancho.suscripciones.data.BillingCycle.MENSUAL) "Mensual" else "Anual",
                style = MaterialTheme.typography.labelMedium,
                color = extra.textMuted,
            )
        }
    }
}

private fun SubscriptionEntity.monthlyEquivalentClpSafe(): Double {
    val monthly = if (cycle == com.pancho.suscripciones.data.BillingCycle.ANUAL) price / 12 else price
    return if (currency == com.pancho.suscripciones.data.Currency.USD) monthly * 950.0 else monthly
}
