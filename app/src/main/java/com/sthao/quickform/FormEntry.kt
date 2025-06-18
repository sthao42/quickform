package com.sthao.quickform

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single database entry for a form.
 * The table name is "form_entries".
 * NOTE: The single image properties have been removed and are now handled
 * in the separate FormImage entity.
 */
@Entity(tableName = "form_entries")
data class FormEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryTitle: String = "",

    // Pickup details
    val pickupRun: String?,
    val pickupDate: String?,
    val pickupDriverName: String?,
    val pickupDriverNumber: String?,
    val pickupFacilityName: String?,
    val pickupFrozenBags: String?,
    val pickupFrozenQuantity: String?,
    val pickupRefrigeratedBags: String?,
    val pickupRefrigeratedQuantity: String?,
    val pickupRoomTempBags: String?,
    val pickupRoomTempQuantity: String?,
    val pickupOthersBags: String?,
    val pickupOthersQuantity: String?,
    val pickupBoxesQuantity: String?,
    val pickupColoredBagsQuantity: String?,
    val pickupMailsQuantity: String?,
    val pickupMoneyBagsQuantity: String?,
    val pickupNotes: String?,
    val pickupPrintSignature: String?,
    val pickupSignature: ByteArray?, // Stored as BLOB

    // Drop-off details
    val dropoffRun: String?,
    val dropoffDate: String?,
    val dropoffDriverName: String?,
    val dropoffDriverNumber: String?,
    val dropoffFacilityName: String?,
    val dropoffFrozenBags: String?,
    val dropoffFrozenQuantity: String?,
    val dropoffRefrigeratedBags: String?,
    val dropoffRefrigeratedQuantity: String?,
    val dropoffRoomTempBags: String?,
    val dropoffRoomTempQuantity: String?,
    val dropoffOthersBags: String?,
    val dropoffOthersQuantity: String?,
    val dropoffBoxesQuantity: String?,
    val dropoffColoredBagsQuantity: String?,
    val dropoffMailsQuantity: String?,
    val dropoffMoneyBagsQuantity: String?,
    val dropoffNotes: String?,
    val dropoffPrintSignature: String?,
    val dropoffSignature: ByteArray? // Stored as BLOB
)