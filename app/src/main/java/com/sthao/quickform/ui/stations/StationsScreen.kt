package com.sthao.quickform.ui.stations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.sthao.quickform.ui.viewmodel.FormEvent
import com.sthao.quickform.ui.viewmodel.FormFieldType
import com.sthao.quickform.ui.viewmodel.FormSection
import com.sthao.quickform.ui.viewmodel.StationsUiState
import com.sthao.quickform.ui.components.SignatureBox
import com.sthao.quickform.ui.theme.stationsForm
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StationsScreen(
    stationsState: StationsUiState,
    onEvent: (FormEvent) -> Unit,
    customTextFieldColors: TextFieldColors,
    runNumber: String,
    onRunNumberChange: (String) -> Unit,
) {
    // Manages the visibility state of the date picker dialog.
    var dateDialogOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormat = remember { SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()) }
    
    // Manage item sections
    var itemSections = remember { mutableStateListOf(StationsItemSection()) }
    
    // Manage selected sections for deletion
    var selectedSections by remember { mutableStateOf(setOf<Int>()) }
    var isInSelectionMode by remember { mutableStateOf(false) }
    
    // Handle back button press to exit selection mode
    BackHandler(enabled = isInSelectionMode) {
        selectedSections = emptySet()
        isInSelectionMode = false
    }
    
    Card(
        modifier =
            Modifier
                .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.stationsForm),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header text for the form.
            Text(
                "Stations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(16.dp))

            // Interaction source to detect when the read-only date field is clicked.
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            // Opens the date dialog when the text field is pressed.
            if (isPressed) {
                LaunchedEffect(Unit) {
                    dateDialogOpen = true
                }
            }

            Column(
                modifier = Modifier
                    .border(BorderStroke(1.5.dp, Color.Gray), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                // A read-only text field to display the selected date.
                OutlinedTextField(
                    value = stationsState.date,
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    interactionSource = interactionSource,
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors,
                )
                // The actual DatePickerDialog composable.
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
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = stationsState.driverName,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.DRIVER_NAME, it)) },
                    label = { Text("Driver Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = stationsState.driverNumber,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.DRIVER_NUMBER, it)) },
                    label = { Text("Driver Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = customTextFieldColors,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = stationsState.run,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.RUN, it)) },
                    label = { Text("Run #") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = customTextFieldColors,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = stationsState.facilityName,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.STATIONS, FormFieldType.FACILITY_NAME, it)) },
                    label = { Text("Facility Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors,
                )
            }
            Spacer(Modifier.height(16.dp))

            // Render item sections
            itemSections.forEachIndexed { index, section ->
                ItemSection(
                    section = section,
                    index = index,
                    isSelected = isInSelectionMode && selectedSections.contains(index),
                    customTextFieldColors = customTextFieldColors,
                    onTotesChange = { value -> 
                        val updatedSections = itemSections.toMutableList().apply {
                            set(index, section.copy(totes = value))
                        }
                        itemSections.clear()
                        itemSections.addAll(updatedSections)
                    },
                    onAddOnsChange = { value -> 
                        val updatedSections = itemSections.toMutableList().apply {
                            set(index, section.copy(addOns = value))
                        }
                        itemSections.clear()
                        itemSections.addAll(updatedSections)
                    },
                    onExtraChange = { value -> 
                        val updatedSections = itemSections.toMutableList().apply {
                            set(index, section.copy(extra = value))
                        }
                        itemSections.clear()
                        itemSections.addAll(updatedSections)
                    },
                    onPrintNameChange = { value ->
                        val updatedSections = itemSections.toMutableList().apply {
                            set(index, section.copy(printName = value))
                        }
                        itemSections.clear()
                        itemSections.addAll(updatedSections)
                    },
                    onSignatureChange = { bitmap ->
                        val updatedSections = itemSections.toMutableList().apply {
                            set(index, section.copy(signature = bitmap))
                        }
                        itemSections.clear()
                        itemSections.addAll(updatedSections)
                    },
                    onLongClick = {
                        if (!isInSelectionMode) {
                            isInSelectionMode = true
                            selectedSections = setOf(index)
                        } else {
                            if (selectedSections.contains(index)) {
                                selectedSections = selectedSections - index
                            } else {
                                selectedSections = selectedSections + index
                            }
                        }
                    },
                    onClick = {
                        if (isInSelectionMode) {
                            if (selectedSections.contains(index)) {
                                selectedSections = selectedSections - index
                            } else {
                                selectedSections = selectedSections + index
                            }
                            
                            // Exit selection mode if no items are selected
                            if (selectedSections.isEmpty()) {
                                isInSelectionMode = false
                            }
                        }
                    }
                )
                
                // Add separator and button after each section except the last one
                if (index < itemSections.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                }
            }
            
            // Add separator and button after the last section
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            
            // Action bar for selected items
            if (isInSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${selectedSections.size} selected")
                    
                    IconButton(
                        onClick = {
                            // Delete selected sections (in reverse order to maintain indices)
                            val sortedIndices = selectedSections.sortedDescending()
                            val updatedSections = itemSections.toMutableList()
                            sortedIndices.forEach { index ->
                                if (index < updatedSections.size) {
                                    updatedSections.removeAt(index)
                                }
                            }
                            itemSections.clear()
                            itemSections.addAll(updatedSections)
                            
                            // Reset selection
                            selectedSections = emptySet()
                            isInSelectionMode = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Selected"
                        )
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
            
            // Add section button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        val newSection = StationsItemSection(id = itemSections.size)
                        itemSections.add(newSection)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Section",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemSection(
    section: StationsItemSection,
    index: Int,
    isSelected: Boolean = false,
    customTextFieldColors: TextFieldColors,
    onTotesChange: (String) -> Unit,
    onAddOnsChange: (String) -> Unit,
    onExtraChange: (String) -> Unit,
    onPrintNameChange: (String) -> Unit,
    onSignatureChange: (android.graphics.Bitmap?) -> Unit,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Text(
        text = "Items ${index + 1}",
        style = MaterialTheme.typography.titleLarge,
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .border(
                BorderStroke(1.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .then(if (isSelected) Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Totes:", modifier = Modifier.weight(1f))
            OutlinedTextField(
                value = section.totes,
                onValueChange = { if (it.all(Char::isDigit)) onTotesChange(it) },
                label = { Text("Quantity") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = customTextFieldColors,
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Add-ons:", modifier = Modifier.weight(1f))
            OutlinedTextField(
                value = section.addOns,
                onValueChange = { if (it.all(Char::isDigit)) onAddOnsChange(it) },
                label = { Text("Quantity") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = customTextFieldColors,
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Extra:", modifier = Modifier.weight(1f))
            OutlinedTextField(
                value = section.extra,
                onValueChange = { if (it.all(Char::isDigit)) onExtraChange(it) },
                label = { Text("Quantity") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = customTextFieldColors,
            )
        }
        Spacer(Modifier.height(8.dp))
        
        // Print Name
        OutlinedTextField(
            value = section.printName,
            onValueChange = onPrintNameChange,
            label = { Text("Print Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = customTextFieldColors,
        )
        Spacer(Modifier.height(8.dp))

        // Signature Box
        SignatureBox(
            label = "Signature",
            bitmap = section.signature,
            onBitmapChange = onSignatureChange,
        )
    }
}
