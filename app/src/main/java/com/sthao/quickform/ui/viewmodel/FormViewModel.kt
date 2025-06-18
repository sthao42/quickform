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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Represents the state for the Pickup screen.
data class PickupUiState(
    val run: String = "",
    val date: String = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()).format(Date()),
    val driverName: String = "",
    val driverNumber: String = "",
    val facilityName: String = "",
    val frozenBags: String = "",
    val frozenQuantity: String = "",
    val refrigeratedBags: String = "",
    val refrigeratedQuantity: String = "",
    val roomTempBags: String = "",
    val roomTempQuantity: String = "",
    val othersBags: String = "",
    val othersQuantity: String = "",
    val boxesQuantity: String = "",
    val coloredBagsQuantity: String = "",
    val mailsQuantity: String = "",
    val moneyBagsQuantity: String = "",
    val notes: String = "",
    val printSignature: String = "",
    val signature: Bitmap? = null,
    val images: List<Uri> = emptyList(),
)

// Represents the state for the Dropoff screen.
data class DropoffUiState(
    val run: String = "",
    val date: String = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()).format(Date()),
    val driverName: String = "",
    val driverNumber: String = "",
    val facilityName: String = "",
    val frozenBags: String = "",
    val frozenQuantity: String = "",
    val refrigeratedBags: String = "",
    val refrigeratedQuantity: String = "",
    val roomTempBags: String = "",
    val roomTempQuantity: String = "",
    val othersBags: String = "",
    val othersQuantity: String = "",
    val boxesQuantity: String = "",
    val coloredBagsQuantity: String = "",
    val mailsQuantity: String = "",
    val moneyBagsQuantity: String = "",
    val notes: String = "",
    val printSignature: String = "",
    val signature: Bitmap? = null,
    val images: List<Uri> = emptyList(),
)

class FormViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FormRepository
    // Exposes the list of saved forms for the third screen.
    val savedForms: StateFlow<List<FormListItem>>

    // REFACTOR: Granular StateFlows for each screen's state to optimize Compose recomposition.
    private val _pickupState = MutableStateFlow(PickupUiState())
    val pickupState: StateFlow<PickupUiState> = _pickupState.asStateFlow()

    private val _dropoffState = MutableStateFlow(DropoffUiState())
    val dropoffState: StateFlow<DropoffUiState> = _dropoffState.asStateFlow()

    // Internal state to track the ID of a form that has been loaded for editing.
    private val _loadedFormId = MutableStateFlow<Long?>(null)

    init {
        // Initializes the database and repository.
        val formDao = FormDatabase.getDatabase(application).formDao()
        repository = FormRepository(formDao)
        savedForms = repository.allFormListItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList(),
        )
    }

    // Central entry point for all UI actions.
    fun onEvent(event: FormEvent) {
        // REFACTOR: The 'when' block now updates the specific state flow, which is more efficient.
        when(event) {
            // Pickup Events
            is FormEvent.UpdatePickupRun -> _pickupState.update { it.copy(run = event.value) }
            is FormEvent.UpdatePickupDate -> _pickupState.update { it.copy(date = event.value) }
            is FormEvent.UpdatePickupDriverName -> _pickupState.update { it.copy(driverName = event.value) }
            is FormEvent.UpdatePickupDriverNumber -> _pickupState.update { it.copy(driverNumber = event.value) }
            is FormEvent.UpdatePickupFacilityName -> _pickupState.update { it.copy(facilityName = event.value) }
            is FormEvent.UpdatePickupFrozenBags -> _pickupState.update { it.copy(frozenBags = event.value) }
            is FormEvent.UpdatePickupFrozenQuantity -> _pickupState.update { it.copy(frozenQuantity = event.value) }
            is FormEvent.UpdatePickupRefrigeratedBags -> _pickupState.update { it.copy(refrigeratedBags = event.value) }
            is FormEvent.UpdatePickupRefrigeratedQuantity -> _pickupState.update { it.copy(refrigeratedQuantity = event.value) }
            is FormEvent.UpdatePickupRoomTempBags -> _pickupState.update { it.copy(roomTempBags = event.value) }
            is FormEvent.UpdatePickupRoomTempQuantity -> _pickupState.update { it.copy(roomTempQuantity = event.value) }
            is FormEvent.UpdatePickupOthersBags -> _pickupState.update { it.copy(othersBags = event.value) }
            is FormEvent.UpdatePickupOthersQuantity -> _pickupState.update { it.copy(othersQuantity = event.value) }
            is FormEvent.UpdatePickupBoxesQuantity -> _pickupState.update { it.copy(boxesQuantity = event.value) }
            is FormEvent.UpdatePickupColoredBagsQuantity -> _pickupState.update { it.copy(coloredBagsQuantity = event.value) }
            is FormEvent.UpdatePickupMailsQuantity -> _pickupState.update { it.copy(mailsQuantity = event.value) }
            is FormEvent.UpdatePickupMoneyBagsQuantity -> _pickupState.update { it.copy(moneyBagsQuantity = event.value) }
            is FormEvent.UpdatePickupNotes -> _pickupState.update { it.copy(notes = event.value) }
            is FormEvent.UpdatePickupPrintSignature -> _pickupState.update { it.copy(printSignature = event.value) }
            is FormEvent.UpdatePickupSignature -> _pickupState.update { it.copy(signature = event.bitmap) }
            is FormEvent.AddPickupImage -> _pickupState.update { it.copy(images = it.images + event.uri) }
            is FormEvent.RemovePickupImage -> _pickupState.update { it.copy(images = it.images - event.uri) }

            // Dropoff Events
            is FormEvent.UpdateDropoffRun -> _dropoffState.update { it.copy(run = event.value) }
            is FormEvent.UpdateDropoffDate -> _dropoffState.update { it.copy(date = event.value) }
            is FormEvent.UpdateDropoffDriverName -> _dropoffState.update { it.copy(driverName = event.value) }
            is FormEvent.UpdateDropoffDriverNumber -> _dropoffState.update { it.copy(driverNumber = event.value) }
            is FormEvent.UpdateDropoffFacilityName -> _dropoffState.update { it.copy(facilityName = event.value) }
            is FormEvent.UpdateDropoffFrozenBags -> _dropoffState.update { it.copy(frozenBags = event.value) }
            is FormEvent.UpdateDropoffFrozenQuantity -> _dropoffState.update { it.copy(frozenQuantity = event.value) }
            is FormEvent.UpdateDropoffRefrigeratedBags -> _dropoffState.update { it.copy(refrigeratedBags = event.value) }
            is FormEvent.UpdateDropoffRefrigeratedQuantity -> _dropoffState.update { it.copy(refrigeratedQuantity = event.value) }
            is FormEvent.UpdateDropoffRoomTempBags -> _dropoffState.update { it.copy(roomTempBags = event.value) }
            is FormEvent.UpdateDropoffRoomTempQuantity -> _dropoffState.update { it.copy(roomTempQuantity = event.value) }
            is FormEvent.UpdateDropoffOthersBags -> _dropoffState.update { it.copy(othersBags = event.value) }
            is FormEvent.UpdateDropoffOthersQuantity -> _dropoffState.update { it.copy(othersQuantity = event.value) }
            is FormEvent.UpdateDropoffBoxesQuantity -> _dropoffState.update { it.copy(boxesQuantity = event.value) }
            is FormEvent.UpdateDropoffColoredBagsQuantity -> _dropoffState.update { it.copy(coloredBagsQuantity = event.value) }
            is FormEvent.UpdateDropoffMailsQuantity -> _dropoffState.update { it.copy(mailsQuantity = event.value) }
            is FormEvent.UpdateDropoffMoneyBagsQuantity -> _dropoffState.update { it.copy(moneyBagsQuantity = event.value) }
            is FormEvent.UpdateDropoffNotes -> _dropoffState.update { it.copy(notes = event.value) }
            is FormEvent.UpdateDropoffPrintSignature -> _dropoffState.update { it.copy(printSignature = event.value) }
            is FormEvent.UpdateDropoffSignature -> _dropoffState.update { it.copy(signature = event.bitmap) }
            is FormEvent.AddDropoffImage -> _dropoffState.update { it.copy(images = it.images + event.uri) }
            is FormEvent.RemoveDropoffImage -> _dropoffState.update { it.copy(images = it.images - event.uri) }

            // Form Actions
            is FormEvent.LoadForm -> loadForm(event.formWithImages)
            is FormEvent.ClearForm -> clearForm()
            is FormEvent.SaveOrUpdateForm -> saveOrUpdateForm(event.context)
            is FormEvent.DeleteFormsByIds -> viewModelScope.launch {
                repository.deleteByIds(event.ids.toList())
            }
        }
    }

    // Fetches the complete details for a set of form IDs.
    suspend fun getFullFormsByIds(ids: Set<Long>): List<FormEntryWithImages> {
        return repository.getFormsWithImagesByIds(ids.toList())
    }

    // Fetches the complete details for a single form ID.
    fun getFormById(id: Long): Flow<FormEntryWithImages> {
        return repository.getFormWithImagesById(id)
    }

    // Gathers data from the state holders and saves a new form or updates an existing one.
    private fun saveOrUpdateForm(context: Context) = viewModelScope.launch {
        // REFACTOR: Reads values from the new, separate state holders.
        val currentPickupState = _pickupState.value
        val currentDropoffState = _dropoffState.value

        if (currentPickupState.facilityName.isBlank() && currentDropoffState.facilityName.isBlank()) {
            Toast.makeText(context, "Cannot save, facility name is empty.", Toast.LENGTH_SHORT).show()
            return@launch
        }

        val dateForTitle = currentPickupState.date.ifBlank {
            SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()).format(Date())
        }
        val formsToday = repository.getFormsByDate(dateForTitle).first()
        val nextId = (formsToday.size + 1).toString().padStart(3, '0')
        val title = "$dateForTitle-$nextId"

        val pickupSignatureBytes = currentPickupState.signature?.let { bitmapToPngByteArray(it) }
        val dropoffSignatureBytes = currentDropoffState.signature?.let { bitmapToPngByteArray(it) }

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
            pickupOthersBags = currentPickupState.othersBags,
            pickupOthersQuantity = currentPickupState.othersQuantity,
            pickupBoxesQuantity = currentPickupState.boxesQuantity,
            pickupColoredBagsQuantity = currentPickupState.coloredBagsQuantity,
            pickupMailsQuantity = currentPickupState.mailsQuantity,
            pickupMoneyBagsQuantity = currentPickupState.moneyBagsQuantity,
            pickupNotes = currentPickupState.notes,
            pickupPrintSignature = currentPickupState.printSignature,
            pickupSignature = pickupSignatureBytes,
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
            dropoffOthersBags = currentDropoffState.othersBags,
            dropoffOthersQuantity = currentDropoffState.othersQuantity,
            dropoffBoxesQuantity = currentDropoffState.boxesQuantity,
            dropoffColoredBagsQuantity = currentDropoffState.coloredBagsQuantity,
            dropoffMailsQuantity = currentDropoffState.mailsQuantity,
            dropoffMoneyBagsQuantity = currentDropoffState.moneyBagsQuantity,
            dropoffNotes = currentDropoffState.notes,
            dropoffPrintSignature = currentDropoffState.printSignature,
            dropoffSignature = dropoffSignatureBytes,
        )

        val imagesToSave = mutableListOf<FormImage>()
        currentPickupState.images.forEach { uri ->
            downsampleImageFromUri(context, uri, 1024)?.let { imageData ->
                imagesToSave.add(FormImage(imageType = "PICKUP", imageData = imageData, formEntryId = 0))
            }
        }
        currentDropoffState.images.forEach { uri ->
            downsampleImageFromUri(context, uri, 1024)?.let { imageData ->
                imagesToSave.add(FormImage(imageType = "DROPOFF", imageData = imageData, formEntryId = 0))
            }
        }

        repository.saveFormWithImages(formToSave, imagesToSave)

        Toast.makeText(context, "Entry Saved!", Toast.LENGTH_SHORT).show()
        clearForm()
    }

    // Populates the UI state with data from a saved form entry.
    private fun loadForm(formWithImages: FormEntryWithImages) {
        val context = getApplication<Application>().applicationContext
        val form = formWithImages.formEntry

        val pickupSignatureBitmap = form.pickupSignature?.let { byteArrayToBitmap(it) }
        val dropoffSignatureBitmap = form.dropoffSignature?.let { byteArrayToBitmap(it) }

        val pickupImageUris = formWithImages.images
            .filter { it.imageType == "PICKUP" }
            .mapNotNull { byteArrayToUri(context, it.imageData, "pickup_${it.id}.png") }

        val dropoffImageUris = formWithImages.images
            .filter { it.imageType == "DROPOFF" }
            .mapNotNull { byteArrayToUri(context, it.imageData, "dropoff_${it.id}.png") }

        // REFACTOR: Updates the individual state holders instead of one large state object.
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
            othersBags = form.pickupOthersBags ?: "",
            othersQuantity = form.pickupOthersQuantity ?: "",
            boxesQuantity = form.pickupBoxesQuantity ?: "",
            coloredBagsQuantity = form.pickupColoredBagsQuantity ?: "",
            mailsQuantity = form.pickupMailsQuantity ?: "",
            moneyBagsQuantity = form.pickupMoneyBagsQuantity ?: "",
            notes = form.pickupNotes ?: "",
            printSignature = form.pickupPrintSignature ?: "",
            signature = pickupSignatureBitmap,
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
            othersBags = form.dropoffOthersBags ?: "",
            othersQuantity = form.dropoffOthersQuantity ?: "",
            boxesQuantity = form.dropoffBoxesQuantity ?: "",
            coloredBagsQuantity = form.dropoffColoredBagsQuantity ?: "",
            mailsQuantity = form.dropoffMailsQuantity ?: "",
            moneyBagsQuantity = form.dropoffMoneyBagsQuantity ?: "",
            notes = form.dropoffNotes ?: "",
            printSignature = form.dropoffPrintSignature ?: "",
            signature = dropoffSignatureBitmap,
            images = dropoffImageUris
        )
    }

    // Resets the state of both forms to their default empty values.
    private fun clearForm() {
        // REFACTOR: Resets each state holder individually.
        _loadedFormId.value = null
        _pickupState.value = PickupUiState()
        _dropoffState.value = DropoffUiState()
    }
}