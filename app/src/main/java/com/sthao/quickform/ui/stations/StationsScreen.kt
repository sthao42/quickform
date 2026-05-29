package com.sthao.quickform.ui.stations

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sthao.quickform.ui.components.MultiImagePicker
import com.sthao.quickform.ui.components.SignatureBox
import com.sthao.quickform.ui.theme.stationsForm
import com.sthao.quickform.ui.viewmodel.FormEvent
import com.sthao.quickform.ui.viewmodel.FormFieldType
import com.sthao.quickform.ui.viewmodel.FormSection
import com.sthao.quickform.ui.viewmodel.StationsUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StationsScreen(
    stationsState: StationsUiState,
    stationsItemSections: List<StationsItemSection>,
    onEvent: (FormEvent) -> Unit,
    onUpdateItemSection: (Int, StationsItemSection) -> Unit,
    onAddItemSection: () -> Unit,
    onRemoveItemSections: (List<Int>) -> Unit,
) {
    // Manages the visibility state of the date picker dialog.
    var dateDialogOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormat = remember { SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()) }
    
    // Manage selected sections for deletion
    var selectedSections by remember { mutableStateOf(setOf<Int>()) }
    var isInSelectionMode by remember { mutableStateOf(false) }
    
    // Handle back button press to exit selection mode
    BackHandler(enabled = isInSelectionMode) {
        selectedSections = emptySet()
        isInSelectionMode = false
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Basic Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "General Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                LaunchedEffect(isPressed) {
                    if (isPressed) {
                        dateDialogOpen = true
                    }
                }

                OutlinedTextField(
                    value = stationsState.date,
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    interactionSource = interactionSource,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                if (dateDialogOpen) {
                    DatePickerDialog(
                        onDismissRequest = { dateDialogOpen = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.DATE, dateFormat.format(Date(it))))
                                }
                                dateDialogOpen = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { dateDialogOpen = false }) { Text("Cancel") }
                        },
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                OutlinedTextField(
                    value = stationsState.driverName,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.DRIVER_NAME, it)) },
                    label = { Text("Driver Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = stationsState.driverNumber,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.DRIVER_NUMBER, it)) },
                    label = { Text("Driver Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = stationsState.facilityName,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.FACILITY_NAME, it)) },
                    label = { Text("Facility Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Action bar for selected items
        if (isInSelectionMode) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${selectedSections.size} selected", style = MaterialTheme.typography.titleSmall)
                    
                    IconButton(
                        onClick = {
                            onRemoveItemSections(selectedSections.sortedDescending())
                            selectedSections = emptySet()
                            isInSelectionMode = false
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Selected")
                    }
                }
            }
        }

        // Render item sections
        stationsItemSections.forEachIndexed { index, section ->
            ItemSection(
                section = section,
                index = index,
                isSelected = isInSelectionMode && selectedSections.contains(index),
                onRunNumberChange = { value -> 
                    onUpdateItemSection(index, section.copy(sectionRunNumber = value))
                },
                onTotesChange = { value -> 
                    onUpdateItemSection(index, section.copy(totes = value))
                },
                onAddOnsChange = { value -> 
                    onUpdateItemSection(index, section.copy(addOns = value))
                },
                onExtraChange = { value -> 
                    onUpdateItemSection(index, section.copy(extra = value))
                },
                onPrintNameChange = { value ->
                    onUpdateItemSection(index, section.copy(printName = value))
                },
                onSignatureChange = { bitmap ->
                    onUpdateItemSection(index, section.copy(signature = bitmap))
                },
                onImageAdded = { uri ->
                    val updatedImages = section.images + uri
                    onUpdateItemSection(index, section.copy(images = updatedImages))
                },
                onImageRemoved = { uri ->
                    val updatedImages = section.images - uri
                    onUpdateItemSection(index, section.copy(images = updatedImages))
                },
                onLongClick = {
                    if (!isInSelectionMode) {
                        isInSelectionMode = true
                        selectedSections = setOf(index)
                    } else {
                        selectedSections = if (selectedSections.contains(index)) {
                            selectedSections - index
                        } else {
                            selectedSections + index
                        }
                    }
                },
                onClick = {
                    if (isInSelectionMode) {
                        selectedSections = if (selectedSections.contains(index)) {
                            selectedSections - index
                        } else {
                            selectedSections + index
                        }
                        
                        if (selectedSections.isEmpty()) {
                            isInSelectionMode = false
                        }
                    }
                }
            )
        }
        
        // Add section button
        Button(
            onClick = onAddItemSection,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Station Section")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemSection(
    section: StationsItemSection,
    index: Int,
    isSelected: Boolean = false,
    onRunNumberChange: (String) -> Unit,
    onTotesChange: (String) -> Unit,
    onAddOnsChange: (String) -> Unit,
    onExtraChange: (String) -> Unit,
    onPrintNameChange: (String) -> Unit,
    onSignatureChange: (android.graphics.Bitmap?) -> Unit,
    onImageAdded: (Uri) -> Unit,
    onImageRemoved: (Uri) -> Unit,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                             else MaterialTheme.colorScheme.stationsForm
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Station Items ${index + 1}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = section.sectionRunNumber,
                onValueChange = onRunNumberChange,
                label = { Text("Run #, Station") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Totes:", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = section.totes,
                    onValueChange = { if (it.all(Char::isDigit)) onUpdateTotes(it, onTotesChange) },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Add-ons:", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = section.addOns,
                    onValueChange = { if (it.all(Char::isDigit)) onUpdateAddOns(it, onAddOnsChange) },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Extra:", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = section.extra,
                    onValueChange = { if (it.all(Char::isDigit)) onUpdateExtra(it, onExtraChange) },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            MultiImagePicker(
                images = section.images,
                onImageAdded = onImageAdded,
                onImageRemoved = onImageRemoved,
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = section.printName,
                onValueChange = onPrintNameChange,
                label = { Text("Print Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            SignatureBox(
                label = "Signature",
                bitmap = section.signature,
                onBitmapChange = onSignatureChange,
            )
        }
    }
}

private fun onUpdateTotes(it: String, onTotesChange: (String) -> Unit) {
    onTotesChange(it)
}

private fun onUpdateAddOns(it: String, onAddOnsChange: (String) -> Unit) {
    onAddOnsChange(it)
}

private fun onUpdateExtra(it: String, onExtraChange: (String) -> Unit) {
    onExtraChange(it)
}
