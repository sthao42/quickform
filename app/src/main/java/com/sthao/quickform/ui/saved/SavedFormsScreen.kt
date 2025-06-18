package com.sthao.quickform.ui.saved

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sthao.quickform.FormEntryWithImages
import com.sthao.quickform.FormListItem
import com.sthao.quickform.ui.components.SelectionAppBar
import com.sthao.quickform.ui.viewmodel.FormEvent
import com.sthao.quickform.ui.viewmodel.FormViewModel
import com.sthao.quickform.util.exportMultipleFormsAsPdf
import com.sthao.quickform.util.shareFormsAsPdf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Defines the UI for the screen that displays all saved forms.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedFormsScreen(
    formViewModel: FormViewModel,
    onEntryClick: (FormEntryWithImages) -> Unit,
) {
    // FIX: Updated to use 'savedForms' to match the refactored ViewModel.
    val forms by formViewModel.savedForms.collectAsState()
    // Manages the set of currently selected form IDs.
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
    // Manages the visibility of the delete confirmation dialog.
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isInSelectionMode = selectedIds.isNotEmpty()

    // Displays a confirmation dialog before deleting forms.
    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            count = selectedIds.size,
            onConfirm = {
                formViewModel.onEvent(FormEvent.DeleteFormsByIds(selectedIds))
                selectedIds = emptySet()
                showDeleteConfirmDialog = false
            },
            onDismiss = {
                showDeleteConfirmDialog = false
            }
        )
    }

    // The main layout scaffold, which switches the top app bar based on selection mode.
    Scaffold(
        topBar = {
            if (isInSelectionMode) {
                // Shows the contextual action bar when items are selected.
                SelectionAppBar(
                    selectedCount = selectedIds.size,
                    onClearSelection = { selectedIds = emptySet() },
                    onDelete = {
                        showDeleteConfirmDialog = true
                    },
                    onShare = {
                        scope.launch {
                            val formsToShare = formViewModel.getFullFormsByIds(selectedIds)
                            shareFormsAsPdf(
                                context = context,
                                forms = formsToShare,
                                scope = scope
                            )
                        }
                    },
                    onExportPdf = {
                        scope.launch {
                            val formsToExport = formViewModel.getFullFormsByIds(selectedIds)
                                .mapIndexed { index, formWithImages -> formWithImages to (index + 1).toString() }
                            exportMultipleFormsAsPdf(context, formsToExport)
                        }
                    }
                )
            } else {
                // Shows the default top app bar.
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Saved Forms",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    modifier = Modifier.height(56.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                )
            }
        }
    ) { paddingValues ->
        // Shows a message if there are no saved forms.
        if (forms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved forms yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Displays the list of saved forms using a performant LazyColumn.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top= 6.dp, bottom = 80.dp)
            ) {
                items(forms, key = { it.id }) { formListItem ->
                    FormEntryItem(
                        formListItem = formListItem,
                        isSelected = formListItem.id in selectedIds,
                        isInSelectionMode = isInSelectionMode,
                        onClick = {
                            if (isInSelectionMode) {
                                // Toggles selection for the item in selection mode.
                                selectedIds = if (it.id in selectedIds) {
                                    selectedIds - it.id
                                } else {
                                    selectedIds + it.id
                                }
                            } else {
                                // Fetches the full form details on demand before navigating.
                                scope.launch {
                                    val fullEntry = formViewModel.getFormById(it.id).first()
                                    onEntryClick(fullEntry)
                                }
                            }
                        },
                        onLongClick = {
                            // Enters selection mode on long click.
                            selectedIds = selectedIds + it.id
                        }
                    )
                }
            }
        }
    }
}

// A single item in the saved forms list.
@Composable
private fun FormEntryItem(
    formListItem: FormListItem,
    isSelected: Boolean,
    isInSelectionMode: Boolean,
    onClick: (FormListItem) -> Unit,
    onLongClick: (FormListItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = { onClick(formListItem) },
                onLongClick = { onLongClick(formListItem) }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Form Entry",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formListItem.entryTitle.ifBlank { "Untitled Entry" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                val runText = "Run#: ${formListItem.pickupRun.ifBlank { "N/A" }}"
                val facilityText = "Facility: ${formListItem.pickupFacilityName.ifBlank { "N/A" }}"

                Text(
                    text = "$runText | $facilityText",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Shows a checkbox if the user is in selection mode.
            if (isInSelectionMode) {
                Spacer(Modifier.width(8.dp))
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick(formListItem) }
                )
            }
        }
    }
}

// A confirmation dialog for deleting items.
@Composable
private fun DeleteConfirmationDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = {
            val entryText = if (count == 1) "entry" else "entries"
            Text("Are you sure you want to permanently delete $count selected $entryText?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}