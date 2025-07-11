package com.sthao.quickform.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sthao.quickform.FormDatabase
import com.sthao.quickform.FormEntry
import com.sthao.quickform.FormEntryWithImages
import com.sthao.quickform.FormImage
import com.sthao.quickform.FormListItem
import com.sthao.quickform.FormRepository
import com.sthao.quickform.util.bitmapToPngByteArray
import com.sthao.quickform.util.byteArrayToBitmap
import com.sthao.quickform.util.byteArrayToUri
import com.sthao.quickform.util.downsampleImageFromUri
import com.sthao.quickform.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.sthao.quickform.util.Constants.DATE_FORMAT_DISPLAY
import com.sthao.quickform.util.Constants.FORM_TITLE_ID_PADDING
import com.sthao.quickform.util.Constants.IMAGE_TYPE_DROPOFF
import com.sthao.quickform.util.Constants.IMAGE_TYPE_PICKUP
import com.sthao.quickform.util.Constants.MIN_FACILITY_NAME_LENGTH
import com.sthao.quickform.util.Constants.STATEFLOW_SUBSCRIPTION_TIMEOUT
import com.sthao.quickform.util.Constants.TOAST_ENTRY_SAVED
import com.sthao.quickform.util.Constants.TOAST_FACILITY_NAME_EMPTY
import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Represents the state for the Pickup screen.
@Immutable
data class PickupUiState(
    val run: String = "",
    val date: String = SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault()).format(Date()),
    val driverName: String = "",
    val driverNumber: String = "",
    val facilityName: String = "",
    val frozenBags: String = "",
    val frozenQuantity: String = "",
    val refrigeratedBags: String = "",
    val refrigeratedQuantity: String = "",
    val roomTempBags: String = "",
    val roomTempQuantity: String = "",
    val boxesQuantity: String = "",
    val coloredBagsQuantity: String = "",
    val mailsQuantity: String = "",
    val moneyBagsQuantity: String = "",
    val othersQuantity: String = "",
    val notes: String = "",
    val additionalNotes: String = "",
    val printSignatureOne: String = "",
    val printSignatureTwo: String = "",
    val signatureOne: Bitmap? = null,
    val signatureTwo: Bitmap? = null,
    val images: List<Uri> = emptyList(),
)

// Represents the state for the Dropoff screen.
@Immutable
data class DropoffUiState(
    val run: String = "",
    val date: String = SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault()).format(Date()),
    val driverName: String = "",
    val driverNumber: String = "",
    val facilityName: String = "",
    val frozenBags: String = "",
    val frozenQuantity: String = "",
    val refrigeratedBags: String = "",
    val refrigeratedQuantity: String = "",
    val roomTempBags: String = "",
    val roomTempQuantity: String = "",
    val boxesQuantity: String = "",
    val coloredBagsQuantity: String = "",
    val mailsQuantity: String = "",
    val moneyBagsQuantity: String = "",
    val othersQuantity: String = "",
    val notes: String = "",
    val additionalNotes: String = "",
    val printSignatureOne: String = "",
    val printSignatureTwo: String = "",
    val signatureOne: Bitmap? = null,
    val signatureTwo: Bitmap? = null,
    val images: List<Uri> = emptyList(),
)

class FormViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FormRepository
    val savedForms: StateFlow<List<FormListItem>>

    private val _pickupState = MutableStateFlow(PickupUiState())
    val pickupState: StateFlow<PickupUiState> = _pickupState.asStateFlow()

    private val _dropoffState = MutableStateFlow(DropoffUiState())
    val dropoffState: StateFlow<DropoffUiState> = _dropoffState.asStateFlow()

    private val _loadedFormId = MutableStateFlow<Long?>(null)

    init {
        val formDao = FormDatabase.getDatabase(application).formDao()
        repository = FormRepository(formDao)
        savedForms = repository.allFormListItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIPTION_TIMEOUT),
            initialValue = emptyList(),
        )
    }

    fun onEvent(event: FormEvent) {
        when(event) {
            // Generalized field update events
            is FormEvent.UpdateField -> when(event.section) {
                FormSection.PICKUP -> _pickupState.update { updateField(it, event.fieldType, event.value) }
                FormSection.DROPOFF -> _dropoffState.update { updateField(it, event.fieldType, event.value) }
            }
            
            // Generalized signature update events
            is FormEvent.UpdateSignature -> when(event.signatureIndex) {
                1 -> when(event.section) {
                    FormSection.PICKUP -> _pickupState.update { it.copy(signatureOne = event.bitmap) }
                    FormSection.DROPOFF -> _dropoffState.update { it.copy(signatureOne = event.bitmap) }
                }
                2 -> when(event.section) {
                    FormSection.PICKUP -> _pickupState.update { it.copy(signatureTwo = event.bitmap) }
                    FormSection.DROPOFF -> _dropoffState.update { it.copy(signatureTwo = event.bitmap) }
                }
            }

            // Generalized image manipulation events
            is FormEvent.AddImage -> when(event.section) {
                FormSection.PICKUP -> _pickupState.update { it.copy(images = it.images + event.uri) }
                FormSection.DROPOFF -> _dropoffState.update { it.copy(images = it.images + event.uri) }
            }
            is FormEvent.RemoveImage -> when(event.section) {
                FormSection.PICKUP -> _pickupState.update { it.copy(images = it.images - event.uri) }
                FormSection.DROPOFF -> _dropoffState.update { it.copy(images = it.images - event.uri) }
            }

            // Form Actions
            is FormEvent.LoadForm -> loadForm(event.formWithImages)
            is FormEvent.ClearForm -> clearForm()
            is FormEvent.SaveOrUpdateForm -> saveOrUpdateForm(event.context)
            is FormEvent.DeleteFormsByIds -> viewModelScope.launch {
                repository.deleteByIds(event.ids.toList())
            }
        }
    }

    // Helper function to update specific fields based on field type
    private fun updateField(pickupState: PickupUiState, fieldType: FormFieldType, value: String): PickupUiState {
        return when (fieldType) {
            FormFieldType.RUN -> pickupState.copy(run = value)
            FormFieldType.DATE -> pickupState.copy(date = value)
            FormFieldType.DRIVER_NAME -> pickupState.copy(driverName = value)
            FormFieldType.DRIVER_NUMBER -> pickupState.copy(driverNumber = value)
            FormFieldType.FACILITY_NAME -> pickupState.copy(facilityName = value)
            FormFieldType.FROZEN_BAGS -> pickupState.copy(frozenBags = value)
            FormFieldType.FROZEN_QUANTITY -> pickupState.copy(frozenQuantity = value)
            FormFieldType.REFRIGERATED_BAGS -> pickupState.copy(refrigeratedBags = value)
            FormFieldType.REFRIGERATED_QUANTITY -> pickupState.copy(refrigeratedQuantity = value)
            FormFieldType.ROOM_TEMP_BAGS -> pickupState.copy(roomTempBags = value)
            FormFieldType.ROOM_TEMP_QUANTITY -> pickupState.copy(roomTempQuantity = value)
            FormFieldType.BOXES_QUANTITY -> pickupState.copy(boxesQuantity = value)
            FormFieldType.COLORED_BAGS_QUANTITY -> pickupState.copy(coloredBagsQuantity = value)
            FormFieldType.MAILS_QUANTITY -> pickupState.copy(mailsQuantity = value)
            FormFieldType.MONEY_BAGS_QUANTITY -> pickupState.copy(moneyBagsQuantity = value)
            FormFieldType.OTHERS_QUANTITY -> pickupState.copy(othersQuantity = value)
            FormFieldType.NOTES -> pickupState.copy(notes = value)
            FormFieldType.ADDITIONAL_NOTES -> pickupState.copy(additionalNotes = value)
            FormFieldType.PRINT_SIGNATURE_ONE -> pickupState.copy(printSignatureOne = value)
            FormFieldType.PRINT_SIGNATURE_TWO -> pickupState.copy(printSignatureTwo = value)
        }
    }

    // Helper function to update specific fields for dropoff state
    private fun updateField(dropoffState: DropoffUiState, fieldType: FormFieldType, value: String): DropoffUiState {
        return when (fieldType) {
            FormFieldType.RUN -> dropoffState.copy(run = value)
            FormFieldType.DATE -> dropoffState.copy(date = value)
            FormFieldType.DRIVER_NAME -> dropoffState.copy(driverName = value)
            FormFieldType.DRIVER_NUMBER -> dropoffState.copy(driverNumber = value)
            FormFieldType.FACILITY_NAME -> dropoffState.copy(facilityName = value)
            FormFieldType.FROZEN_BAGS -> dropoffState.copy(frozenBags = value)
            FormFieldType.FROZEN_QUANTITY -> dropoffState.copy(frozenQuantity = value)
            FormFieldType.REFRIGERATED_BAGS -> dropoffState.copy(refrigeratedBags = value)
            FormFieldType.REFRIGERATED_QUANTITY -> dropoffState.copy(refrigeratedQuantity = value)
            FormFieldType.ROOM_TEMP_BAGS -> dropoffState.copy(roomTempBags = value)
            FormFieldType.ROOM_TEMP_QUANTITY -> dropoffState.copy(roomTempQuantity = value)
            FormFieldType.BOXES_QUANTITY -> dropoffState.copy(boxesQuantity = value)
            FormFieldType.COLORED_BAGS_QUANTITY -> dropoffState.copy(coloredBagsQuantity = value)
            FormFieldType.MAILS_QUANTITY -> dropoffState.copy(mailsQuantity = value)
            FormFieldType.MONEY_BAGS_QUANTITY -> dropoffState.copy(moneyBagsQuantity = value)
            FormFieldType.OTHERS_QUANTITY -> dropoffState.copy(othersQuantity = value)
            FormFieldType.NOTES -> dropoffState.copy(notes = value)
            FormFieldType.ADDITIONAL_NOTES -> dropoffState.copy(additionalNotes = value)
            FormFieldType.PRINT_SIGNATURE_ONE -> dropoffState.copy(printSignatureOne = value)
            FormFieldType.PRINT_SIGNATURE_TWO -> dropoffState.copy(printSignatureTwo = value)
        }
    }

    suspend fun getFullFormsByIds(ids: Set<Long>): List<FormEntryWithImages> {
        return repository.getFormsWithImagesByIds(ids.toList())
    }

    fun getFormById(id: Long): Flow<FormEntryWithImages> {
        return repository.getFormWithImagesById(id)
    }

    private fun saveOrUpdateForm(context: Context) = viewModelScope.launch {
        val currentPickupState = _pickupState.value
        val currentDropoffState = _dropoffState.value

        if (currentPickupState.facilityName.length < MIN_FACILITY_NAME_LENGTH && 
            currentDropoffState.facilityName.length < MIN_FACILITY_NAME_LENGTH) {
            Toast.makeText(context, TOAST_FACILITY_NAME_EMPTY, Toast.LENGTH_SHORT).show()
            return@launch
        }

        val dateForTitle = currentPickupState.date.ifBlank {
            SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault()).format(Date())
        }
        val formsToday = repository.getFormsByDate(dateForTitle).first()
        val nextId = (formsToday.size + 1).toString().padStart(FORM_TITLE_ID_PADDING, '0')
        val title = "$dateForTitle-$nextId"

        val pickupSignatureOneBytes = currentPickupState.signatureOne?.let { bitmapToPngByteArray(it) }
        val pickupSignatureTwoBytes = currentPickupState.signatureTwo?.let { bitmapToPngByteArray(it) }
        val dropoffSignatureOneBytes = currentDropoffState.signatureOne?.let { bitmapToPngByteArray(it) }
        val dropoffSignatureTwoBytes = currentDropoffState.signatureTwo?.let { bitmapToPngByteArray(it) }

        val formToSave = FormEntry(
            id = _loadedFormId.value ?: 0,
            entryTitle = title,
            pickupRun = currentPickupState.run,
            pickupDate = currentPickupState.date,
            pickupDriverName = currentPickupState.driverName,
            pickupDriverNumber = currentPickupState.driverNumber,
            pickupFacilityName = currentPickupState.facilityName,
            pickupFrozenBags = currentPickupState.frozenBags,
            pickupFrozenQuantity = currentPickupState.frozenQuantity,
            pickupRefrigeratedBags = currentPickupState.refrigeratedBags,
            pickupRefrigeratedQuantity = currentPickupState.refrigeratedQuantity,
            pickupRoomTempBags = currentPickupState.roomTempBags,
            pickupRoomTempQuantity = currentPickupState.roomTempQuantity,
            pickupBoxesQuantity = currentPickupState.boxesQuantity,
            pickupColoredBagsQuantity = currentPickupState.coloredBagsQuantity,
            pickupMailsQuantity = currentPickupState.mailsQuantity,
            pickupMoneyBagsQuantity = currentPickupState.moneyBagsQuantity,
            pickupOthersQuantity = currentPickupState.othersQuantity,
            pickupNotes = currentPickupState.notes,
            pickupAdditionalNotes = currentPickupState.additionalNotes,
            pickupPrintSignatureOne = currentPickupState.printSignatureOne,
            pickupPrintSignatureTwo = currentPickupState.printSignatureTwo,
            pickupSignatureOne = pickupSignatureOneBytes,
            pickupSignatureTwo = pickupSignatureTwoBytes,
            dropoffRun = currentDropoffState.run,
            dropoffDate = currentDropoffState.date,
            dropoffDriverName = currentDropoffState.driverName,
            dropoffDriverNumber = currentDropoffState.driverNumber,
            dropoffFacilityName = currentDropoffState.facilityName,
            dropoffFrozenBags = currentDropoffState.frozenBags,
            dropoffFrozenQuantity = currentDropoffState.frozenQuantity,
            dropoffRefrigeratedBags = currentDropoffState.refrigeratedBags,
            dropoffRefrigeratedQuantity = currentDropoffState.refrigeratedQuantity,
            dropoffRoomTempBags = currentDropoffState.roomTempBags,
            dropoffRoomTempQuantity = currentDropoffState.roomTempQuantity,
            dropoffBoxesQuantity = currentDropoffState.boxesQuantity,
            dropoffColoredBagsQuantity = currentDropoffState.coloredBagsQuantity,
            dropoffMailsQuantity = currentDropoffState.mailsQuantity,
            dropoffMoneyBagsQuantity = currentDropoffState.moneyBagsQuantity,
            dropoffOthersQuantity = currentDropoffState.othersQuantity,
            dropoffNotes = currentDropoffState.notes,
            dropoffAdditionalNotes = currentDropoffState.additionalNotes,
            dropoffPrintSignatureOne = currentDropoffState.printSignatureOne,
            dropoffPrintSignatureTwo = currentDropoffState.printSignatureTwo,
            dropoffSignatureOne = dropoffSignatureOneBytes,
            dropoffSignatureTwo = dropoffSignatureTwoBytes
        )

        val imagesToSave = mutableListOf<FormImage>()
        currentPickupState.images.forEach { uri ->
            downsampleImageFromUri(context, uri, Constants.IMAGE_MAX_DIMENSION)?.let { imageData ->
                imagesToSave.add(FormImage(imageType = IMAGE_TYPE_PICKUP, imageData = imageData, formEntryId = 0))
            }
        }
        currentDropoffState.images.forEach { uri ->
            downsampleImageFromUri(context, uri, Constants.IMAGE_MAX_DIMENSION)?.let { imageData ->
                imagesToSave.add(FormImage(imageType = IMAGE_TYPE_DROPOFF, imageData = imageData, formEntryId = 0))
            }
        }

        repository.saveFormWithImages(formToSave, imagesToSave)

        Toast.makeText(context, TOAST_ENTRY_SAVED, Toast.LENGTH_SHORT).show()
        clearForm()
    }

    private fun loadForm(formWithImages: FormEntryWithImages) {
        val context = getApplication<Application>().applicationContext
        val form = formWithImages.formEntry

        val pickupSignatureOneBitmap = form.pickupSignatureOne?.let { byteArrayToBitmap(it) }
        val pickupSignatureTwoBitmap = form.pickupSignatureTwo?.let { byteArrayToBitmap(it) }
        val dropoffSignatureOneBitmap = form.dropoffSignatureOne?.let { byteArrayToBitmap(it) }
        val dropoffSignatureTwoBitmap = form.dropoffSignatureTwo?.let { byteArrayToBitmap(it) }


        val pickupImageUris = formWithImages.images
            .filter { it.imageType == IMAGE_TYPE_PICKUP }
            .mapNotNull { byteArrayToUri(context, it.imageData, "pickup_${it.id}${Constants.PNG_EXTENSION}") }

        val dropoffImageUris = formWithImages.images
            .filter { it.imageType == IMAGE_TYPE_DROPOFF }
            .mapNotNull { byteArrayToUri(context, it.imageData, "dropoff_${it.id}${Constants.PNG_EXTENSION}") }

        _loadedFormId.value = form.id

        _pickupState.value = PickupUiState(
            run = form.pickupRun ?: "",
            date = form.pickupDate ?: "",
            driverName = form.pickupDriverName ?: "",
            driverNumber = form.pickupDriverNumber ?: "",
            facilityName = form.pickupFacilityName ?: "",
            frozenBags = form.pickupFrozenBags ?: "",
            frozenQuantity = form.pickupFrozenQuantity ?: "",
            refrigeratedBags = form.pickupRefrigeratedBags ?: "",
            refrigeratedQuantity = form.pickupRefrigeratedQuantity ?: "",
            roomTempBags = form.pickupRoomTempBags ?: "",
            roomTempQuantity = form.pickupRoomTempQuantity ?: "",
            boxesQuantity = form.pickupBoxesQuantity ?: "",
            coloredBagsQuantity = form.pickupColoredBagsQuantity ?: "",
            mailsQuantity = form.pickupMailsQuantity ?: "",
            moneyBagsQuantity = form.pickupMoneyBagsQuantity ?: "",
            othersQuantity = form.pickupOthersQuantity ?: "",
            notes = form.pickupNotes ?: "",
            additionalNotes = form.pickupAdditionalNotes ?: "",
            printSignatureOne = form.pickupPrintSignatureOne ?: "",
            printSignatureTwo = form.pickupPrintSignatureTwo ?: "",
            signatureOne = pickupSignatureOneBitmap,
            signatureTwo = pickupSignatureTwoBitmap,
            images = pickupImageUris
        )

        _dropoffState.value = DropoffUiState(
            run = form.dropoffRun ?: "",
            date = form.dropoffDate ?: "",
            driverName = form.dropoffDriverName ?: "",
            driverNumber = form.dropoffDriverNumber ?: "",
            facilityName = form.dropoffFacilityName ?: "",
            frozenBags = form.dropoffFrozenBags ?: "",
            frozenQuantity = form.dropoffFrozenQuantity ?: "",
            refrigeratedBags = form.dropoffRefrigeratedBags ?: "",
            refrigeratedQuantity = form.dropoffRefrigeratedQuantity ?: "",
            roomTempBags = form.dropoffRoomTempBags ?: "",
            roomTempQuantity = form.dropoffRoomTempQuantity ?: "",
            boxesQuantity = form.dropoffBoxesQuantity ?: "",
            coloredBagsQuantity = form.dropoffColoredBagsQuantity ?: "",
            mailsQuantity = form.dropoffMailsQuantity ?: "",
            moneyBagsQuantity = form.dropoffMoneyBagsQuantity ?: "",
            othersQuantity = form.dropoffOthersQuantity ?: "",
            notes = form.dropoffNotes ?: "",
            additionalNotes = form.dropoffAdditionalNotes ?: "",
            printSignatureOne = form.dropoffPrintSignatureOne ?: "",
            printSignatureTwo = form.dropoffPrintSignatureTwo ?: "",
            signatureOne = dropoffSignatureOneBitmap,
            signatureTwo = dropoffSignatureTwoBitmap,
            images = dropoffImageUris
        )
    }

    private fun clearForm() {
        _loadedFormId.value = null
        _pickupState.value = PickupUiState()
        _dropoffState.value = DropoffUiState()
    }
}