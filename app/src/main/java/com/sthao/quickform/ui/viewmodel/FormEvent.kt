package com.sthao.quickform.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.sthao.quickform.FormEntryWithImages

/**
 * Enum representing different form field types for more type-safe field updates.
 */
enum class FormFieldType {
    // Basic info fields
    RUN, DATE, DRIVER_NAME, DRIVER_NUMBER, FACILITY_NAME, NOTES,
    
    // Quantity fields
    FROZEN_BAGS, FROZEN_QUANTITY,
    REFRIGERATED_BAGS, REFRIGERATED_QUANTITY,
    ROOM_TEMP_BAGS, ROOM_TEMP_QUANTITY,
    BOXES_QUANTITY, COLORED_BAGS_QUANTITY,
    MAILS_QUANTITY, MONEY_BAGS_QUANTITY, OTHERS_QUANTITY,
    
    // Signature fields
    PRINT_SIGNATURE_ONE, PRINT_SIGNATURE_TWO
}

/**
 * Enum representing form sections (pickup vs dropoff).
 */
enum class FormSection {
    PICKUP, DROPOFF
}

/**
 * All possible events that can happen in the form UI.
 * Refactored for better type safety and reduced boilerplate.
 */
sealed class FormEvent {
    
    // Generalized field update events
    data class UpdateField(
        val section: FormSection,
        val fieldType: FormFieldType,
        val value: String
    ) : FormEvent()
    
    data class UpdateSignature(
        val section: FormSection,
        val signatureIndex: Int, // 1 or 2
        val bitmap: Bitmap?
    ) : FormEvent()
    
    data class AddImage(
        val section: FormSection,
        val uri: Uri
    ) : FormEvent()
    
    data class RemoveImage(
        val section: FormSection,
        val uri: Uri
    ) : FormEvent()


    // Form Actions
    data class LoadForm(val formWithImages: FormEntryWithImages) : FormEvent()
    data object ClearForm : FormEvent()
    data class SaveOrUpdateForm(val context: Context) : FormEvent()
    data class DeleteFormsByIds(val ids: Set<Long>) : FormEvent()
}
