package com.pancho.suscripciones.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pancho.suscripciones.data.CategoryEntity
import com.pancho.suscripciones.ui.AppViewModel
import com.pancho.suscripciones.ui.monoForName
import com.pancho.suscripciones.ui.theme.LocalExtraColors
import com.pancho.suscripciones.ui.theme.LogoPalette

@Composable
fun CategoryEditScreen(
    viewModel: AppViewModel,
    categoryId: Long?,
    onDone: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val existing = categoryId?.let { id -> state.categories.map { it.category }.firstOrNull { it.id == id } }
    val extra = LocalExtraColors.current

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var color by remember { mutableStateOf(existing?.colorHex ?: "#5B8CFF") }
    var confirmDelete by remember { mutableStateOf(false) }
    val subCount = state.categories.firstOrNull { it.category.id == categoryId }?.subs?.size ?: 0

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text(
            if (existing == null) "Agregar categoría" else "Editar categoría",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(android.graphics.Color.parseColor(color)), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(monoForName(name.ifBlank { "?" }), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        )

        Text(
            "Color",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LogoPalette.forEach { swatch ->
                val hex = "#%06X".format(swatch.toArgb() and 0xFFFFFF)
                val selected = hex.equals(color, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(swatch, CircleShape)
                        .border(if (selected) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        .clickable { color = hex },
                )
            }
        }

        Button(
            enabled = name.isNotBlank(),
            onClick = {
                val entity = CategoryEntity(
                    id = existing?.id ?: 0,
                    name = name,
                    colorHex = color,
                    mono = monoForName(name),
                )
                viewModel.saveCategory(entity)
                onDone()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        ) { Text("Guardar categoría") }

        if (existing != null) {
            if (!confirmDelete) {
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = extra.danger),
                ) { Text("Eliminar categoría") }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, extra.danger, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Text(
                        if (subCount > 0) "También se eliminarán sus $subCount suscripciones." else "¿Eliminar esta categoría?",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { confirmDelete = false }, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        Button(
                            onClick = { viewModel.deleteCategory(existing); onDone() },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = extra.danger),
                        ) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}
