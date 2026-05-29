package com.sthao.quickform.ui.pickup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.sthao.quickform.ui.components.ItemRow
import com.sthao.quickform.ui.components.MultiImagePicker
import com.sthao.quickform.ui.components.QuantityRow
import com.sthao.quickform.ui.components.SignatureBox
import com.sthao.quickform.ui.theme.pickupForm
import com.sthao.quickform.ui.viewmodel.FormEvent
import com.sthao.quickform.ui.viewmodel.FormFieldType
import com.sthao.quickform.ui.viewmodel.FormSection
import com.sthao.quickform.ui.viewmodel.PickupUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Defines the entire UI for the "Pick Up" form screen.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupScreen(
    state: PickupUiState,
    onEvent: (FormEvent) -> Unit,
    runNumber: String,
    onRunNumberChange: (String) -> Unit,
) {
    // Manages the visibility state of the date picker dialog.
    var dateDialogOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormat = remember { SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()) }

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

                // Interaction source to detect when the read-only date field is clicked.
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                // Opens the date dialog when the text field is pressed.
                if (isPressed) {
                    LaunchedEffect(Unit) {
                        dateDialogOpen = true
                    }
                }

                OutlinedTextField(
                    value = state.date,
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
                                    onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.DATE, dateFormat.format(Date(it))))
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
                    value = state.driverName,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.DRIVER_NAME, it)) },
                    label = { Text("Driver Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.driverNumber,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.DRIVER_NUMBER, it)) },
                    label = { Text("Driver Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = runNumber,
                    onValueChange = { if (it.all(Char::isDigit)) onRunNumberChange(it) },
                    label = { Text("Run #") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.facilityName,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.FACILITY_NAME, it)) },
                    label = { Text("Facility Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Section: Items
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.pickupForm),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Items Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ItemRow("Frozen", state.frozenBags, state.frozenQuantity) { bags, qty ->
                    if (bags != null) onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.FROZEN_BAGS, bags))
                    if (qty != null) onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.FROZEN_QUANTITY, qty))
                }

                ItemRow("Refrigerated", state.refrigeratedBags, state.refrigeratedQuantity) { bags, qty ->
                    if (bags != null) onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.REFRIGERATED_BAGS, bags))
                    if (qty != null) onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.REFRIGERATED_QUANTITY, qty))
                }

                ItemRow("Room Temp", state.roomTempBags, state.roomTempQuantity) { bags, qty ->
                    if (bags != null) onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.ROOM_TEMP_BAGS, bags))
                    if (qty != null) onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.ROOM_TEMP_QUANTITY, qty))
                }

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.NOTES, it)) },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Section: Signatures 1
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Dispatcher Signature",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = state.printSignatureOne,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.PRINT_SIGNATURE_ONE, it)) },
                    label = { Text("Print Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                SignatureBox(
                    label = "Signature",
                    bitmap = state.signatureOne,
                    onBitmapChange = { bmp -> onEvent(FormEvent.UpdateSignature(FormSection.PICKUP, 1, bmp)) },
                )
            }
        }

        // Section: Additional Items
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Additional Supplies",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                QuantityRow("Boxes", state.boxesQuantity) { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.BOXES_QUANTITY, it)) }
                QuantityRow("Colored Bags", state.coloredBagsQuantity) { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.COLORED_BAGS_QUANTITY, it)) }
                QuantityRow("Mails", state.mailsQuantity) { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.MAILS_QUANTITY, it)) }
                QuantityRow("Money Bags", state.moneyBagsQuantity) { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.MONEY_BAGS_QUANTITY, it)) }
                QuantityRow("Others", state.othersQuantity) { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.OTHERS_QUANTITY, it)) }

                OutlinedTextField(
                    value = state.additionalNotes,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.ADDITIONAL_NOTES, it)) },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Section: Images
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        ) {
            Column(Modifier.padding(16.dp)) {
                MultiImagePicker(
                    images = state.images,
                    onImageAdded = { uri -> onEvent(FormEvent.AddImage(FormSection.PICKUP, uri)) },
                    onImageRemoved = { uri -> onEvent(FormEvent.RemoveImage(FormSection.PICKUP, uri)) }
                )
            }
        }

        // Section: Signatures 2
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Driver Signature",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = state.printSignatureTwo,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.PICKUP, FormFieldType.PRINT_SIGNATURE_TWO, it)) },
                    label = { Text("Print Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                SignatureBox(
                    label = "Signature",
                    bitmap = state.signatureTwo,
                    onBitmapChange = { bmp -> onEvent(FormEvent.UpdateSignature(FormSection.PICKUP, 2, bmp)) },
                )
            }
        }
    }
}


