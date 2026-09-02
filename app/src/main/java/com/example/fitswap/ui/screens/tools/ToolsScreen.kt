package com.example.fitswap.ui.screens.tools

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.MenuNavigationIcon
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.launch

private const val EXPORT_FILE_NAME = "fitswap-backup.json"

@Composable
fun ToolsScreen(
    onMenuClick: () -> Unit,
    viewModel: ToolsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = viewModel.buildExportJson()
                context.contentResolver.openOutputStream(uri)?.use { stream -> stream.write(json.toByteArray()) }
                viewModel.onExportSucceeded()
            } catch (e: Exception) {
                viewModel.onExportFailed(e.message ?: "no se pudo escribir el archivo")
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).readText()
                }
            } catch (e: Exception) {
                null
            }
            if (text != null) {
                viewModel.importFromJson(text)
            } else {
                viewModel.onImportFailed("no se pudo leer el archivo")
            }
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(title = "Herramientas", navigationIcon = { MenuNavigationIcon(onMenuClick = onMenuClick) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Backup de tus datos", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { exportLauncher.launch(EXPORT_FILE_NAME) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exportButton")
            ) { Text("Exportar") }
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("importButton")
            ) { Text("Importar") }
        }
    }
}
