package com.pancho.suscripciones.ui.detail

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pancho.suscripciones.data.BillingCycle
import com.pancho.suscripciones.data.Currency
import com.pancho.suscripciones.ui.AppViewModel
import com.pancho.suscripciones.ui.theme.LocalExtraColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SubscriptionDetailScreen(
    viewModel: AppViewModel,
    subscriptionId: Long,
    onBack: () -> Unit,
    onEdit: (categoryId: Long, subscriptionId: Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val sub = state.categories.flatMap { it.subs }.firstOrNull { it.id == subscriptionId } ?: return
    val category = state.categories.firstOrNull { it.category.id == sub.categoryId }?.category
    val extra = LocalExtraColors.current

    var showPassword by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val color = Color(android.graphics.Color.parseColor(sub.logoColorHex))

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Detalle", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            val logoBitmap = remember(sub.logoImagePath) { sub.logoImagePath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() } }
            Box(
                modifier = Modifier.size(64.dp).background(color, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (logoBitmap != null) {
                    Image(bitmap = logoBitmap, contentDescription = null, modifier = Modifier.size(40.dp))
                } else {
                    Text(sub.logoLetter, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
            }
            Text(
                sub.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 10.dp),
            )
            category?.let {
                Box(
                    modifier = Modifier
                        .background(extra.accentSoft, RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) { Text(it.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp),
        ) {
            DetailRow("Precio", "$${sub.price.toLong()} ${sub.currency}" + if (sub.currency == Currency.USD) "  (≈ $${(sub.price * com.pancho.suscripciones.ui.USD_CLP_RATE).toLong()} CLP)" else "")
            DetailRow("Ciclo de pago", if (sub.cycle == BillingCycle.MENSUAL) "Mensual" else "Anual")
            DetailRow("Próximo cobro", LocalDate.ofEpochDay(sub.nextChargeDateEpochDay).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("es"))))
            DetailRow("Método de pago", sub.paymentMethod)
            DetailRow("Sitio web", sub.website)
            DetailRow("Usuario", sub.username)
            DetailRow(
                "Contraseña",
                if (showPassword) viewModel.repository.decryptPassword(sub) else "•".repeat(8),
                trailing = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Mostrar/ocultar",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                isLast = true,
            )
        }

        Button(
            onClick = { category?.let { onEdit(it.id, sub.id) } },
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) { Text("Editar suscripción") }

        if (!confirmDelete) {
            OutlinedButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = extra.danger),
            ) { Text("Eliminar suscripción") }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .border(1.dp, extra.danger, RoundedCornerShape(16.dp))
                    .padding(16.dp),
            ) {
                Text(
                    "¿Eliminar esta suscripción? Esta acción no se puede deshacer.",
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { confirmDelete = false }, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(
                        onClick = { viewModel.deleteSubscription(sub); onBack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = extra.danger),
                    ) { Text("Eliminar") }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, trailing: (@Composable () -> Unit)? = null, isLast: Boolean = false) {
    val extra = LocalExtraColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .then(if (!isLast) Modifier.border(0.dp, extra.textMuted) else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = extra.textMuted, style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            trailing?.invoke()
        }
    }
}
