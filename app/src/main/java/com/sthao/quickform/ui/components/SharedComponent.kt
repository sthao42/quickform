package com.sthao.quickform.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import coil.compose.rememberAsyncImagePainter
import com.sthao.quickform.R
import com.sthao.quickform.util.Constants

private object Dimens {
    val SignatureBoxHeight = Constants.SIGNATURE_BOX_HEIGHT_DP.dp
    val ImagePreviewSize = Constants.IMAGE_PREVIEW_SIZE_DP.dp
    val DialogCanvasHeight = Constants.DIALOG_CANVAS_HEIGHT_DP.dp
    val LogoHeight = Constants.LOGO_HEIGHT_DP.dp
    val FabSize = Constants.FAB_SIZE_DP.dp
}

@Stable
private class SignatureCanvasState(initialBitmap: Bitmap?) {
    val currentPath = Path()
    val newPaths = mutableStateListOf<Path>()
    var effectiveBitmap by mutableStateOf(initialBitmap)
    var pathTicker by mutableIntStateOf(0)

    val isBlank: Boolean
        get() = effectiveBitmap == null && newPaths.isEmpty()

    fun addPath() {
        newPaths.add(Path().apply { addPath(currentPath) })
        currentPath.reset()
        pathTicker++
    }

    fun clear() {
        newPaths.clear()
        currentPath.reset()
        effectiveBitmap = null
        pathTicker++
    }
}

@Composable
private fun rememberSignatureCanvasState(initialBitmap: Bitmap?): SignatureCanvasState {
    return remember(initialBitmap) {
        SignatureCanvasState(initialBitmap)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBanner() {
    TopAppBar(
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.quickform),
                contentDescription = "Quick Form Logo",
                modifier =
                    Modifier
                        .padding(start = 12.dp)
                        .height(Dimens.LogoHeight),
            )
        },
        title = {
            Text(
                "Confirmation Form",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
    )
}

@Composable
fun DotsIndicator(
    pageCount: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == selectedIndex
            Box(
                modifier =
                    Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (isSelected) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        ),
            )
        }
    }
}

@Composable
fun FabRow(
    onNewEntry: () -> Unit,
    onSaveEntry: () -> Unit,
    onNavigateToSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FloatingActionButton(
            onClick = onNewEntry,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ) { Icon(Icons.Filled.Add, contentDescription = "New Entry") }

        FloatingActionButton(
            onClick = onSaveEntry,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(Dimens.FabSize),
        ) { Icon(Icons.Filled.Done, contentDescription = "Save Entry") }

        FloatingActionButton(
            onClick = onNavigateToSaved,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "View Saved Entries") }
    }
}

@Composable
private fun SignatureCaptureDialog(
    initialBitmap: Bitmap?,
    onSave: (Bitmap?) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberSignatureCanvasState(initialBitmap = initialBitmap)
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceDim,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxWidth()
                        .height(Dimens.DialogCanvasHeight)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        .clipToBounds()
                        .onSizeChanged { boxSize = it }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    focusManager.clearFocus()
                                    state.currentPath.moveTo(offset.x, offset.y)
                                    state.pathTicker++
                                },
                                onDrag = { change, _ ->
                                    change.historical.forEach {
                                        state.currentPath.lineTo(it.position.x, it.position.y)
                                    }
                                    state.currentPath.lineTo(change.position.x, change.position.y)
                                    state.pathTicker++
                                    change.consume()
                                },
                                onDragEnd = { state.addPath() },
                            )
                        },
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        state.pathTicker
                        state.effectiveBitmap?.let { drawImage(it.asImageBitmap()) }

                        state.newPaths.forEach { path ->
                            drawPath(path = path, color = Color.Black, style = Stroke(width = 5f))
                        }
                        drawPath(path = state.currentPath, color = Color.Black, style = Stroke(width = 5f))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Button(onClick = { state.clear() }) {
                        Text("Clear")
                    }
                    Button(
                        onClick = {
                            if (state.isBlank) {
                                onSave(null)
                            } else {
                                val finalBitmap = createBitmap(
                                    width = boxSize.width, height = boxSize.height, config = Bitmap.Config.ARGB_8888,
                                ).applyCanvas {
                                    state.effectiveBitmap?.let { drawBitmap(it, 0f, 0f, null) }
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.BLACK
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 5f
                                        isAntiAlias = true
                                    }
                                    state.newPaths.forEach { path -> drawPath(path.asAndroidPath(), paint) }
                                }
                                onSave(finalBitmap)
                            }
                            onDismiss()
                        },
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun SignatureBox(
    label: String,
    bitmap: Bitmap?,
    onBitmapChange: (Bitmap?) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        SignatureCaptureDialog(
            initialBitmap = bitmap,
            onSave = { onBitmapChange(it) },
            onDismiss = { showDialog = false },
        )
    }

    Column {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .height(Dimens.SignatureBoxHeight)
                .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Saved Signature",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = "Tap to sign",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
fun MultiImagePicker(
    images: List<Uri>,
    onImageAdded: (Uri) -> Unit,
    onImageRemoved: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let(onImageAdded)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Attached Images", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AddImageButton(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                }
            )

            images.forEach { imageUri ->
                ImageThumbnail(
                    uri = imageUri,
                    onRemove = { onImageRemoved(imageUri) }
                )
            }
        }
    }
}

@Composable
private fun ImageThumbnail(
    uri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(Dimens.ImagePreviewSize)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Selected Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                    shape = CircleShape
                ),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Remove Image",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddImageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(Dimens.ImagePreviewSize),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add Image",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text("Add Image", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionAppBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: () -> Unit,
) {
    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Clear Selection")
            }
        },
        actions = {
            IconButton(onClick = onExportPdf) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "Export as PDF")
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            titleContentColor = MaterialTheme.colorScheme.onSecondary,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondary,
            navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
        ),
    )
}