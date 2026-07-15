package com.pancho.suscripciones.ui.options

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pancho.suscripciones.data.BackupManager
import com.pancho.suscripciones.data.PaymentMethodEntity
import com.pancho.suscripciones.ui.AppViewModel
import com.pancho.suscripciones.ui.theme.LocalExtraColors
import kotlinx.coroutines.launch

@Composable
fun OptionsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val extra = LocalExtraColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var newMethodName by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                BackupManager.export(uri, context, viewModel.repository)
                Toast.makeText(context, "Respaldo exportado", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                BackupManager.import(uri, context, viewModel.repository)
                Toast.makeText(context, "Respaldo importado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Opciones", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        }

        Text(
            "Medios de pago",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        state.paymentMethods.forEach { pm ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(pm.name, color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = { viewModel.deletePaymentMethod(pm) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = extra.danger)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newMethodName,
                onValueChange = { newMethodName = it },
                label = { Text("Nuevo medio de pago") },
                modifier = Modifier.weight(1f),
            )
            Button(
                enabled = newMethodName.isNotBlank(),
                onClick = {
                    viewModel.savePaymentMethod(PaymentMethodEntity(name = newMethodName.trim()))
                    newMethodName = ""
                },
            ) { Text("Agregar") }
        }

        Text(
            "Respaldo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp),
        )
        Text(
            "El respaldo incluye tus contraseñas en texto plano. Guárdalo en un lugar privado.",
            style = MaterialTheme.typography.labelMedium,
            color = extra.textMuted,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Button(
            onClick = { exportLauncher.launch("suscripciones_backup.json") },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Exportar respaldo") }
        OutlinedButton(
            onClick = { importLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp),
        ) { Text("Importar respaldo") }
    }
}
