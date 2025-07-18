package com.sthao.quickform.ui.dropoff

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
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sthao.quickform.ui.components.MultiImagePicker
import com.sthao.quickform.ui.components.SignatureBox
import com.sthao.quickform.ui.theme.dropOffForm
import com.sthao.quickform.ui.viewmodel.DropoffUiState
import com.sthao.quickform.ui.viewmodel.FormEvent
import com.sthao.quickform.ui.viewmodel.FormFieldType
import com.sthao.quickform.ui.viewmodel.FormSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Defines the entire UI for the "Drop Off" form screen.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropoffScreen(
    state: DropoffUiState,
    onEvent: (FormEvent) -> Unit,
    customTextFieldColors: TextFieldColors,
    runNumber: String,
    onRunNumberChange: (String) -> Unit,
) {
    // Manages the visibility state of the date picker dialog.
    var dateDialogOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormat = remember { SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()) }

    Card(
        modifier =
            Modifier
                .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.dropOffForm),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header text for the form.
            Text(
                "Drop Off",
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
                    value = state.date,
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
                                    onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.DATE, dateFormat.format(Date(it))))
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

                // A series of text fields for user input.
                OutlinedTextField(
                    value = state.driverName,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.DRIVER_NAME, it)) },
                    label = { Text("Driver Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.driverNumber,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.DRIVER_NUMBER, it)) },
                    label = { Text("Driver Number") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = runNumber,
                    onValueChange = { if (it.all(Char::isDigit)) onRunNumberChange(it) },
                    label = { Text("Run #") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.facilityName,
                    onValueChange = { onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.FACILITY_NAME, it)) },
                    label = { Text("Facility Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors,
                )
            }
            Spacer(Modifier.height(16.dp))

            Text(
                "Items",
                style = MaterialTheme.typography.titleLarge,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .border(BorderStroke(1.5.dp, Color.Gray), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                // Rows of fields for capturing item quantities.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Frozen:", modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.frozenBags,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.FROZEN_BAGS, it)) },
                        label = { Text("Bags") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = state.frozenQuantity,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.FROZEN_QUANTITY, it)) },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Refrigerated:", modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.refrigeratedBags,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.REFRIGERATED_BAGS, it)) },
                        label = { Text("Bags") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = state.refrigeratedQuantity,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.REFRIGERATED_QUANTITY, it)) },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Room Temp:", modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.roomTempBags,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.ROOM_TEMP_BAGS, it)) },
                        label = { Text("Bags") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = state.roomTempQuantity,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.ROOM_TEMP_QUANTITY, it)) },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = { onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.NOTES, it)) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors,
            )
            Spacer(Modifier.height(8.dp))

            // Print Signature One
            OutlinedTextField(
                value = state.printSignatureOne,
                onValueChange = { onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.PRINT_SIGNATURE_ONE, it)) },
                label = { Text("Print Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors,
            )
            Spacer(Modifier.height(8.dp))

            // The shared component for capturing a signature one
            SignatureBox(
                label = "Signature",
                bitmap = state.signatureOne,
                onBitmapChange = { bmp -> onEvent(FormEvent.UpdateSignature(FormSection.DROPOFF, 1, bmp)) },
            )
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .border(BorderStroke(1.5.dp, Color.Gray), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Boxes:", modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.boxesQuantity,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.BOXES_QUANTITY, it)) },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Colored Bags:", modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.coloredBagsQuantity,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.COLORED_BAGS_QUANTITY, it)) },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Mails:", modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.mailsQuantity,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.MAILS_QUANTITY, it)) },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Money Bags:", modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.moneyBagsQuantity,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.MONEY_BAGS_QUANTITY, it)) },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Others:", modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.othersQuantity,
                        onValueChange = { if (it.all(Char::isDigit)) onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.OTHERS_QUANTITY, it)) },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        colors = customTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.additionalNotes,
                onValueChange = { onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.ADDITIONAL_NOTES, it)) },
                label = { Text("Additional Notes") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors,
            )
            Spacer(Modifier.height(16.dp))

            // The shared component for picking multiple images.
            MultiImagePicker(
                images = state.images,
                onImageAdded = { uri -> onEvent(FormEvent.AddImage(FormSection.DROPOFF, uri)) },
                onImageRemoved = { uri -> onEvent(FormEvent.RemoveImage(FormSection.DROPOFF, uri)) }
            )
            Spacer(Modifier.height(16.dp))

            // Print Signature Two
            OutlinedTextField(
                value = state.printSignatureTwo,
                onValueChange = { onEvent(FormEvent.UpdateField(FormSection.DROPOFF, FormFieldType.PRINT_SIGNATURE_TWO, it)) },
                label = { Text("Print Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors,
            )
            Spacer(Modifier.height(8.dp))

            // The shared component for capturing a signature two.
            SignatureBox(
                label = "Signature",
                bitmap = state.signatureTwo,
                onBitmapChange = { bmp -> onEvent(FormEvent.UpdateSignature(FormSection.DROPOFF, 2, bmp)) },
            )
        }
    }
}