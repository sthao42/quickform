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
import com.sthao.quickform.FormEntryWithImagesAndSections
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
import com.sthao.quickform.ui.stations.StationsItemSection
import com.sthao.quickform.StationsItemSectionEntity
import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Represents the state for the Pickup screen.
@Immutable
data class PickupUiState(
    val date: String = SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault()).format(Date()),
    val driverName: String = "",
    val driverNumber: String = "",
    val run: String = "",
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
    val date: String = SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault()).format(Date()),
    val driverName: String = "",
    val driverNumber: String = "",
    val run: String = "",
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

// Represents the state for the Stations screen.
@Immutable
data class StationsUiState(
    val date: String = SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault()).format(Date()),
    val driverName: String = "",
    val driverNumber: String = "",
    val run: String = "",
    val facilityName: String = "",
    val images: List<Uri> = emptyList() // Moved printName, signature, totes, addOns, extra to item sections
)

class FormViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FormRepository
    val savedForms: StateFlow<List<FormListItem>>

    private val _pickupState = MutableStateFlow(PickupUiState())
    val pickupState: StateFlow<PickupUiState> = _pickupState.asStateFlow()

    private val _dropoffState = MutableStateFlow(DropoffUiState())
    val dropoffState: StateFlow<DropoffUiState> = _dropoffState.asStateFlow()

    private val _stationsState = MutableStateFlow(StationsUiState()) // Added for Stations
    val stationsState: StateFlow<StationsUiState> = _stationsState.asStateFlow() // Added for Stations

    private val _stationsItemSections = MutableStateFlow<List<StationsItemSection>>(emptyList()) // Added for Stations Item Sections
    val stationsItemSections: StateFlow<List<StationsItemSection>> = _stationsItemSections.asStateFlow() // Added for Stations Item Sections

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
                FormSection.STATIONS -> _stationsState.update { updateField(it, event.fieldType, event.value) } // Added for Stations
            }

            // Generalized signature update events
            is FormEvent.UpdateSignature -> when(event.signatureIndex) {
                1 -> when(event.section) {
                    FormSection.PICKUP -> _pickupState.update { it.copy(signatureOne = event.bitmap) }
                    FormSection.DROPOFF -> _dropoffState.update { it.copy(signatureOne = event.bitmap) }
                    FormSection.STATIONS -> {} // Removed since signature is now in item sections
                }
                2 -> when(event.section) {
                    FormSection.PICKUP -> _pickupState.update { it.copy(signatureTwo = event.bitmap) }
                    FormSection.DROPOFF -> _dropoffState.update { it.copy(signatureTwo = event.bitmap) }
                    FormSection.STATIONS -> {} // Removed since signature is now in item sections
                }
                else -> {} // Handle unexpected signatureIndex
            }

            // Generalized image manipulation events
            is FormEvent.AddImage -> when(event.section) {
                FormSection.PICKUP -> _pickupState.update { it.copy(images = it.images + event.uri) }
                FormSection.DROPOFF -> _dropoffState.update { it.copy(images = it.images + event.uri) }
                FormSection.STATIONS -> _stationsState.update { it.copy(images = it.images + event.uri) } // Added for Stations
            }
            is FormEvent.RemoveImage -> when(event.section) {
                FormSection.PICKUP -> _pickupState.update { it.copy(images = it.images - event.uri) }
                FormSection.DROPOFF -> _dropoffState.update { it.copy(images = it.images - event.uri) }
                FormSection.STATIONS -> _stationsState.update { it.copy(images = it.images - event.uri) } // Added for Stations
            }

            // Form Actions
            is FormEvent.LoadForm -> loadFormWithSections(event.formWithImages.formEntry.id)
            is FormEvent.LoadFormWithSections -> loadFormWithSections(event.formId)
            is FormEvent.UpdateStationsItemSection -> {
                val currentSections = _stationsItemSections.value.toMutableList()
                if (event.index < currentSections.size) {
                    currentSections[event.index] = event.itemSection
                    _stationsItemSections.value = currentSections
                }
            }
            is FormEvent.AddStationsItemSection -> {
                val currentSections = _stationsItemSections.value.toMutableList()
                val newSection = StationsItemSection(id = currentSections.size)
                currentSections.add(newSection)
                _stationsItemSections.value = currentSections
            }
            is FormEvent.RemoveStationsItemSections -> {
                val currentSections = _stationsItemSections.value.toMutableList()
                // Remove in reverse order to maintain indices
                event.indices.forEach { index ->
                    if (index < currentSections.size) {
                        currentSections.removeAt(index)
                    }
                }
                _stationsItemSections.value = currentSections
            }
            is FormEvent.ClearForm -> clearForm()
            is FormEvent.SaveOrUpdateForm -> saveOrUpdateForm(event.context)
            is FormEvent.DeleteFormsByIds -> viewModelScope.launch {
                repository.deleteByIds(event.ids.toList())
            }
        }
    }

    // Helper function to update specific fields for pickup state
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
            else -> pickupState // Or handle error for unexpected field types
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
            else -> dropoffState // Or handle error for unexpected field types
        }
    }

    // Helper function to update specific fields for stations state // Added for Stations
    private fun updateField(stationsState: StationsUiState, fieldType: FormFieldType, value: String): StationsUiState {
        return when (fieldType) {
            FormFieldType.DATE -> stationsState.copy(date = value)
            FormFieldType.DRIVER_NAME -> stationsState.copy(driverName = value)
            FormFieldType.DRIVER_NUMBER -> stationsState.copy(driverNumber = value)
            FormFieldType.RUN -> stationsState.copy(run = value)
            FormFieldType.FACILITY_NAME -> stationsState.copy(facilityName = value)
            // Add the missing cases for Stations-specific fields
            FormFieldType.TOTES -> stationsState.copy() // Totes is now in item sections
            FormFieldType.ADD_ONS -> stationsState.copy() // Add-ons is now in item sections
            FormFieldType.EXTRA -> stationsState.copy() // Extra is now in item sections
            else -> stationsState // Or handle error for unexpected field types
        }
    }

    suspend fun getFullFormsByIds(ids: Set<Long>): List<FormEntryWithImagesAndSections> {
        return repository.getFormsWithImagesByIds(ids.toList())
    }

    fun getFormWithSectionsById(id: Long): Flow<FormEntryWithImagesAndSections> {
        return repository.getFormWithImagesAndSectionsById(id)
    }

    private fun saveOrUpdateForm(context: Context) = viewModelScope.launch {
        val currentPickupState = _pickupState.value
        val currentDropoffState = _dropoffState.value
        val currentStationsState = _stationsState.value // Added for Stations

        // Updated validation to include Stations facility name
        if (currentPickupState.facilityName.length < MIN_FACILITY_NAME_LENGTH &&
            currentDropoffState.facilityName.length < MIN_FACILITY_NAME_LENGTH &&
            currentStationsState.facilityName.length < MIN_FACILITY_NAME_LENGTH) {
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

        val formType = if (currentStationsState.facilityName.isNotBlank()) "stations" else "pickup_dropoff"

        val formToSave = FormEntry(
            id = _loadedFormId.value ?: 0,
            entryTitle = title,
            formType = formType,
            // Pickup fields
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
            // Dropoff fields
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
            dropoffSignatureTwo = dropoffSignatureTwoBytes,
            // Stations fields - Basic fields only
            stationsRun = currentStationsState.run,
            stationsDate = currentStationsState.date,
            stationsDriverName = currentStationsState.driverName,
            stationsDriverNumber = currentStationsState.driverNumber,
            stationsFacilityName = currentStationsState.facilityName,
            stationsTotes = "", // Moved to item sections
            stationsAddOns = "", // Moved to item sections
            stationsExtra = "", // Moved to item sections
            stationsPrintSignatureOne = "", // Moved to item sections
            stationsSignatureOne = null // Moved to item sections
        )

        val imagesToSave = mutableListOf<FormImage>()
        
        // Helper function to process images
        fun processImages(images: List<Uri>, imageType: String, sectionIndex: Int = -1) {
            images.forEach { uri ->
                downsampleImageFromUri(context, uri, Constants.IMAGE_MAX_DIMENSION)?.let { imageData ->
                    imagesToSave.add(FormImage(
                        imageType = imageType,
                        imageData = imageData,
                        formEntryId = 0,
                        sectionIndex = sectionIndex
                    ))
                }
            }
        }
        
        // Process images for each section
        processImages(currentPickupState.images, IMAGE_TYPE_PICKUP)
        processImages(currentDropoffState.images, IMAGE_TYPE_DROPOFF)
        processImages(currentStationsState.images, Constants.IMAGE_TYPE_STATIONS)
        
        // Add images for each Stations item section
        _stationsItemSections.value.forEachIndexed { index, section ->
            processImages(section.images, Constants.IMAGE_TYPE_STATIONS, index)
        }

        // Convert StationsItemSections to StationsItemSectionEntity for database storage
        val sectionsToSave = _stationsItemSections.value.mapIndexed { index, section ->
            val signatureBytes = section.signature?.let { bitmapToPngByteArray(it) }
            StationsItemSectionEntity(
                id = 0, // ID will be auto-generated
                formEntryId = 0, // Will be set when saving
                sectionIndex = index,
                sectionRunNumber = section.sectionRunNumber,
                totes = section.totes,
                addOns = section.addOns,
                extra = section.extra,
                printName = section.printName,
                signature = signatureBytes
            )
        }

        repository.saveFormWithImagesAndSections(formToSave, imagesToSave, sectionsToSave)

        Toast.makeText(context, TOAST_ENTRY_SAVED, Toast.LENGTH_SHORT).show()
        clearForm()
    }

    

    fun loadFormWithSections(id: Long) {
        viewModelScope.launch {
            try {
                repository.getFormWithImagesAndSectionsById(id).first().let { formWithSections ->
                    loadFormWithSections(formWithSections)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadFormWithSections(formWithSections: FormEntryWithImagesAndSections) {
        val context = getApplication<Application>().applicationContext
        val form = formWithSections.formEntry

        val pickupSignatureOneBitmap = form.pickupSignatureOne?.let { byteArrayToBitmap(it) }
        val pickupSignatureTwoBitmap = form.pickupSignatureTwo?.let { byteArrayToBitmap(it) }
        val dropoffSignatureOneBitmap = form.dropoffSignatureOne?.let { byteArrayToBitmap(it) }
        val dropoffSignatureTwoBitmap = form.dropoffSignatureTwo?.let { byteArrayToBitmap(it) }

        // Helper function to convert images
        fun convertImages(imageType: String, prefix: String): List<Uri> {
            return formWithSections.images
                .filter { it.imageType == imageType && it.sectionIndex == -1 }
                .mapNotNull { byteArrayToUri(context, it.imageData, "${prefix}_${it.id}${Constants.PNG_EXTENSION}") }
        }
        
        val pickupImageUris = convertImages(IMAGE_TYPE_PICKUP, "pickup")
        val dropoffImageUris = convertImages(IMAGE_TYPE_DROPOFF, "dropoff")
        val stationsImageUris = convertImages(Constants.IMAGE_TYPE_STATIONS, "stations")

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

        // Load Stations basic fields
        _stationsState.value = StationsUiState(
            run = form.stationsRun ?: "",
            date = form.stationsDate ?: "",
            driverName = form.stationsDriverName ?: "",
            driverNumber = form.stationsDriverNumber ?: "",
            facilityName = form.stationsFacilityName ?: "",
            images = stationsImageUris
        )

        // Load Stations item sections
        val itemSections = formWithSections.stationsItemSections.map { sectionEntity ->
            val signatureBitmap = sectionEntity.signature?.let { byteArrayToBitmap(it) }
            
            // Get images for this section
            val sectionImages = formWithSections.images
                .filter { it.imageType == Constants.IMAGE_TYPE_STATIONS && it.sectionIndex == sectionEntity.sectionIndex }
                .mapNotNull { byteArrayToUri(context, it.imageData, "stations_${it.id}_section_${sectionEntity.sectionIndex}${Constants.PNG_EXTENSION}") }
            
            StationsItemSection(
                id = sectionEntity.sectionIndex,
                sectionRunNumber = sectionEntity.sectionRunNumber,
                totes = sectionEntity.totes,
                addOns = sectionEntity.addOns,
                extra = sectionEntity.extra,
                printName = sectionEntity.printName,
                signature = signatureBitmap,
                images = sectionImages
            )
        }
        _stationsItemSections.value = itemSections
    }

    private fun clearForm() {
        _loadedFormId.value = null
        _pickupState.value = PickupUiState()
        _dropoffState.value = DropoffUiState()
        _stationsState.value = StationsUiState() // Added for Stations
        _stationsItemSections.value = emptyList() // Added for Stations Item Sections
    }
}