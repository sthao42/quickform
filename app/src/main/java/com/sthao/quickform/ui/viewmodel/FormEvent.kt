package com.sthao.quickform.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.sthao.quickform.FormEntryWithImages

/**
 * Defines all possible user actions (events) that can be sent from the UI to the ViewModel.
 */
sealed interface FormEvent {
    // Pickup Events
    data class UpdatePickupRun(val value: String) : FormEvent
    data class UpdatePickupDate(val value: String) : FormEvent
    data class UpdatePickupDriverName(val value: String) : FormEvent
    data class UpdatePickupDriverNumber(val value: String) : FormEvent
    data class UpdatePickupFacilityName(val value: String) : FormEvent
    data class UpdatePickupFrozenBags(val value: String) : FormEvent
    data class UpdatePickupFrozenQuantity(val value: String) : FormEvent
    data class UpdatePickupRefrigeratedBags(val value: String) : FormEvent
    data class UpdatePickupRefrigeratedQuantity(val value: String) : FormEvent
    data class UpdatePickupRoomTempBags(val value: String) : FormEvent
    data class UpdatePickupRoomTempQuantity(val value: String) : FormEvent
    data class UpdatePickupOthersBags(val value: String) : FormEvent
    data class UpdatePickupOthersQuantity(val value: String) : FormEvent
    data class UpdatePickupBoxesQuantity(val value: String) : FormEvent
    data class UpdatePickupColoredBagsQuantity(val value: String) : FormEvent
    data class UpdatePickupMailsQuantity(val value: String) : FormEvent
    data class UpdatePickupMoneyBagsQuantity(val value: String) : FormEvent
    data class UpdatePickupNotes(val value: String) : FormEvent
    data class UpdatePickupPrintSignature(val value: String) : FormEvent
    data class UpdatePickupSignature(val bitmap: Bitmap?) : FormEvent
    data class AddPickupImage(val uri: Uri) : FormEvent
    data class RemovePickupImage(val uri: Uri) : FormEvent


    // Dropoff Events
    data class UpdateDropoffRun(val value: String) : FormEvent
    data class UpdateDropoffDate(val value: String) : FormEvent
    data class UpdateDropoffDriverName(val value: String) : FormEvent
    data class UpdateDropoffDriverNumber(val value: String) : FormEvent
    data class UpdateDropoffFacilityName(val value: String) : FormEvent
    data class UpdateDropoffFrozenBags(val value: String) : FormEvent
    data class UpdateDropoffFrozenQuantity(val value: String) : FormEvent
    data class UpdateDropoffRefrigeratedBags(val value: String) : FormEvent
    data class UpdateDropoffRefrigeratedQuantity(val value: String) : FormEvent
    data class UpdateDropoffRoomTempBags(val value: String) : FormEvent
    data class UpdateDropoffRoomTempQuantity(val value: String) : FormEvent
    data class UpdateDropoffOthersBags(val value: String) : FormEvent
    data class UpdateDropoffOthersQuantity(val value: String) : FormEvent
    data class UpdateDropoffBoxesQuantity(val value: String) : FormEvent
    data class UpdateDropoffColoredBagsQuantity(val value: String) : FormEvent
    data class UpdateDropoffMailsQuantity(val value: String) : FormEvent
    data class UpdateDropoffMoneyBagsQuantity(val value: String) : FormEvent
    data class UpdateDropoffNotes(val value: String) : FormEvent
    data class UpdateDropoffPrintSignature(val value: String) : FormEvent
    data class UpdateDropoffSignature(val bitmap: Bitmap?) : FormEvent
    data class AddDropoffImage(val uri: Uri) : FormEvent
    data class RemoveDropoffImage(val uri: Uri) : FormEvent


    // Form Actions
    data class LoadForm(val formWithImages: FormEntryWithImages) : FormEvent
    data object ClearForm : FormEvent
    data class SaveOrUpdateForm(val context: Context) : FormEvent
    data class DeleteFormsByIds(val ids: Set<Long>) : FormEvent
}