package com.example.fitswap.ui.screens.exercises

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fitswap.domain.model.GalleryItem
import com.example.fitswap.ui.common.AppTopBar
import com.example.fitswap.ui.common.BackNavigationIcon
import com.example.fitswap.ui.common.ConfirmDialog
import kotlinx.coroutines.launch

@Composable
fun ExerciseFormScreen(
    onClose: () -> Unit,
    viewModel: ExerciseFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val galleryItems by viewModel.galleryItems.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteBlockedMessage by remember { mutableStateOf(false) }
    var deleteMediaId by remember { mutableStateOf<String?>(null) }
    var mediaUrlDraft by remember { mutableStateOf("") }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        // El picker moderno ya entrega acceso persistente por si sola, pero algunos providers
        // solo lo dan si se pide explícitamente — sin esto, la foto puede dejar de poder verse
        // luego de reiniciar la app (el permiso de la URI expira con el proceso).
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        val isVideo = context.contentResolver.getType(uri)?.startsWith("video/") == true
        viewModel.addMedia(uri.toString(), isVideo)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (uiState.isNew) "Nuevo ejercicio" else "Editar ejercicio",
                navigationIcon = { BackNavigationIcon(onBack = onClose) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Nombre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exerciseNameField")
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.muscleGroup,
                    onValueChange = viewModel::onMuscleGroupChanged,
                    label = { Text("Grupo muscular") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exerciseMuscleGroupField")
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.equipment,
                    onValueChange = viewModel::onEquipmentChanged,
                    label = { Text("Equipo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exerciseEquipmentField")
                )
            }

            item {
                Text("Tags (straps, configuraciones, etc.)")
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.tags, key = { it }) { tag ->
                        InputChip(
                            selected = false,
                            onClick = { viewModel.removeTag(tag) },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(Icons.Filled.Close, contentDescription = "Quitar $tag")
                            },
                            modifier = Modifier.testTag("tagChip_$tag")
                        )
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.newTagText,
                        onValueChange = viewModel::onNewTagTextChanged,
                        label = { Text("Nuevo tag") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("newTagField")
                    )
                    OutlinedButton(
                        onClick = viewModel::addTag,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .testTag("addTagButton")
                    ) {
                        Text("Agregar")
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            if (viewModel.save()) onClose()
                        }
                    },
                    enabled = uiState.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saveExerciseButton")
                ) {
                    Text("Guardar")
                }
            }

            if (!uiState.isNew) {
                item {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deleteExerciseButton")
                    ) {
                        Text("Eliminar ejercicio")
                    }
                }
                if (deleteBlockedMessage) {
                    item {
                        Text(
                            text = "No se puede eliminar: está en uso en una rutina.",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("deleteExerciseBlockedLabel")
                        )
                    }
                }

                item {
                    Text("Galería", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                            modifier = Modifier.testTag("addMediaFromDeviceButton")
                        ) {
                            Text("+ Desde el dispositivo")
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = mediaUrlDraft,
                            onValueChange = { mediaUrlDraft = it },
                            label = { Text("Enlace web (imagen o video)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mediaUrlField")
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.addMediaFromUrl(mediaUrlDraft)
                                mediaUrlDraft = ""
                            },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .testTag("addMediaFromUrlButton")
                        ) {
                            Text("Agregar")
                        }
                    }
                }
                if (galleryItems.isEmpty()) {
                    item {
                        Text(
                            text = "Sin fotos ni videos todavía",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.testTag("noGalleryItemsLabel")
                        )
                    }
                } else {
                    items(galleryItems, key = { it.id }) { galleryItem ->
                        GalleryItemRow(
                            item = galleryItem,
                            onDelete = { deleteMediaId = galleryItem.id }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "¿Eliminar ejercicio?",
            message = "Se eliminará junto con su galería, notas e historial. No se puede deshacer.",
            onConfirm = {
                showDeleteConfirm = false
                scope.launch {
                    if (viewModel.deleteExercise()) onClose() else deleteBlockedMessage = true
                }
            },
            onCancel = { showDeleteConfirm = false }
        )
    }

    val mediaIdToDelete = deleteMediaId
    if (mediaIdToDelete != null) {
        ConfirmDialog(
            title = "¿Eliminar este archivo?",
            message = "Se eliminará de la galería del ejercicio. No se puede deshacer.",
            onConfirm = {
                viewModel.deleteMedia(mediaIdToDelete)
                deleteMediaId = null
            },
            onCancel = { deleteMediaId = null }
        )
    }
}

@Composable
private fun GalleryItemRow(item: GalleryItem, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("galleryItem_${item.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(64.dp)) {
            if (item.isVideo) {
                Text("▶ Video", style = MaterialTheme.typography.labelMedium)
            } else {
                AsyncImage(
                    model = item.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                )
            }
        }
        Text(item.uri, modifier = Modifier.weight(1f), maxLines = 1)
        IconButton(onClick = onDelete, modifier = Modifier.testTag("deleteMediaButton_${item.id}")) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
        }
    }
}
