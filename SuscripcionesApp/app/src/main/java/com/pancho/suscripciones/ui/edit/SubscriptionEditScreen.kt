package com.pancho.suscripciones.ui.edit

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pancho.suscripciones.data.BillingCycle
import com.pancho.suscripciones.data.Currency
import com.pancho.suscripciones.data.LogoDownloader
import com.pancho.suscripciones.data.SubscriptionEntity
import com.pancho.suscripciones.ui.AppViewModel
import com.pancho.suscripciones.ui.findBrand
import com.pancho.suscripciones.ui.monoForName
import com.pancho.suscripciones.ui.theme.LocalExtraColors
import com.pancho.suscripciones.ui.theme.LogoPalette
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionEditScreen(
    viewModel: AppViewModel,
    categoryId: Long,
    subscriptionId: Long?,
    onDone: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val existing = subscriptionId?.let { id -> state.categories.flatMap { it.subs }.firstOrNull { it.id == id } }

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var selectedCategoryId by remember { mutableStateOf(existing?.categoryId ?: categoryId) }
    var currency by remember { mutableStateOf(existing?.currency ?: Currency.CLP) }
    var cycle by remember { mutableStateOf(existing?.cycle ?: BillingCycle.MENSUAL) }
    var price by remember { mutableStateOf(existing?.price?.toString() ?: "") }
    var paymentMethod by remember { mutableStateOf(existing?.paymentMethod ?: "") }
    var website by remember { mutableStateOf(existing?.website ?: "") }
    var username by remember { mutableStateOf(existing?.username ?: "") }
    var password by remember { mutableStateOf(existing?.let { viewModel.repository.decryptPassword(it) } ?: "") }
    var showPassword by remember { mutableStateOf(false) }
    var logoColor by remember { mutableStateOf(existing?.logoColorHex ?: "#5B8CFF") }
    var logoImagePath by remember { mutableStateOf(existing?.logoImagePath) }
    var isDownloadingLogo by remember { mutableStateOf(false) }
    var nextChargeDate by remember {
        mutableStateOf(existing?.nextChargeDateEpochDay ?: LocalDate.now().plusMonths(1).toEpochDay())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extra = LocalExtraColors.current
    val brand = remember(name) { findBrand(name) }

    // Al escribir el nombre de un servicio reconocido (solo en suscripciones nuevas), autocompleta
    // el color de marca real y el sitio web si aún no se han editado manualmente.
    LaunchedEffect(brand) {
        if (existing == null && brand != null) {
            if (logoColor == "#5B8CFF") logoColor = brand.colorHex
            if (website.isBlank()) website = brand.domain
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Text(
            if (existing == null) "Agregar suscripción" else "Editar suscripción",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())

        Text(
            "Categoría",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.categories.forEach { c ->
                FilterChip(
                    selected = c.category.id == selectedCategoryId,
                    onClick = { selectedCategoryId = c.category.id },
                    label = { Text(c.category.name) },
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Currency.values().forEach { c ->
                FilterChip(selected = currency == c, onClick = { currency = c }, label = { Text(c.name) }, modifier = Modifier.weight(1f))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = cycle == BillingCycle.MENSUAL, onClick = { cycle = BillingCycle.MENSUAL }, label = { Text("Mensual") }, modifier = Modifier.weight(1f))
            FilterChip(selected = cycle == BillingCycle.ANUAL, onClick = { cycle = BillingCycle.ANUAL }, label = { Text("Anual") }, modifier = Modifier.weight(1f))
        }

        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))

        Text(
            "Fecha de cobro",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                LocalDate.ofEpochDay(nextChargeDate).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("es"))),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = LocalDate.ofEpochDay(nextChargeDate)
                    .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            nextChargeDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                        }
                        showDatePicker = false
                    }) { Text("Aceptar") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
            ) { DatePicker(state = datePickerState) }
        }

        Text(
            "Método de pago",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
        )
        if (state.paymentMethods.isEmpty()) {
            Text("Agrega medios de pago desde Opciones.", style = MaterialTheme.typography.labelMedium, color = extra.textMuted)
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.paymentMethods.forEach { pm ->
                    FilterChip(selected = pm.name == paymentMethod, onClick = { paymentMethod = pm.name }, label = { Text(pm.name) })
                }
            }
        }

        OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("Sitio web") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )

        Text(
            "Logo",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val previewPath = logoImagePath
            if (previewPath != null) {
                val bitmap = remember(previewPath) { BitmapFactory.decodeFile(previewPath)?.asImageBitmap() }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(android.graphics.Color.parseColor(logoColor)), RoundedCornerShape(14.dp))
                            .padding(6.dp),
                    )
                }
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(android.graphics.Color.parseColor(logoColor)), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        existing?.logoLetter?.takeIf { it.isNotBlank() } ?: monoForName(name.ifBlank { "?" }).take(1),
                        color = Color.White,
                    )
                }
            }

            if (isDownloadingLogo) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(
                    onClick = {
                        val domain = website.ifBlank { brand?.domain ?: "" }
                        if (domain.isBlank()) return@TextButton
                        scope.launch {
                            isDownloadingLogo = true
                            val downloaded = LogoDownloader.downloadFavicon(context, domain)
                            if (downloaded != null) {
                                LogoDownloader.deleteLogo(logoImagePath)
                                logoImagePath = downloaded
                            }
                            isDownloadingLogo = false
                        }
                    },
                    enabled = website.isNotBlank() || brand != null,
                ) { Text("Buscar logo online") }
            }

            if (logoImagePath != null && !isDownloadingLogo) {
                TextButton(onClick = { LogoDownloader.deleteLogo(logoImagePath); logoImagePath = null }) {
                    Text("Quitar")
                }
            }
        }

        Text(
            "Color del logo",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LogoPalette.forEach { swatch ->
                val hex = "#%06X".format(swatch.toArgb() and 0xFFFFFF)
                val selected = hex.equals(logoColor, ignoreCase = true)
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(swatch, CircleShape)
                        .border(if (selected) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        .then(Modifier.padding(0.dp))
                        .clickableColor { logoColor = hex },
                )
            }
        }

        Button(
            enabled = name.isNotBlank() && selectedCategoryId != -1L,
            onClick = {
                val entity = SubscriptionEntity(
                    id = existing?.id ?: 0,
                    categoryId = selectedCategoryId,
                    name = name,
                    price = price.toDoubleOrNull() ?: 0.0,
                    currency = currency,
                    cycle = cycle,
                    nextChargeDateEpochDay = nextChargeDate,
                    paymentMethod = paymentMethod,
                    website = website,
                    username = username,
                    passwordEncrypted = "",
                    logoColorHex = logoColor,
                    logoLetter = existing?.logoLetter?.takeIf { it.isNotBlank() } ?: monoForName(name).take(1),
                    logoImagePath = logoImagePath,
                )
                viewModel.saveSubscription(entity, password)
                onDone()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        ) { Text("Guardar suscripción") }

        if (existing != null) {
            OutlinedButton(
                onClick = { viewModel.deleteSubscription(existing); onDone() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            ) { Text("Eliminar suscripción") }
        }
    }
}

private fun Modifier.clickableColor(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
