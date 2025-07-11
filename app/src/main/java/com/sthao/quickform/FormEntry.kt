package com.sthao.quickform

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single database entry for a form.
 * NOTE: equals() and hashCode() have been overridden to handle ByteArray properties correctly.
 */
@Entity(
    tableName = "form_entries",
    indices = [
        Index(value = ["entryTitle"]),
        Index(value = ["pickupDate"]),
        Index(value = ["dropoffDate"]),
        Index(value = ["pickupFacilityName"]),
        Index(value = ["dropoffFacilityName"])
    ]
)
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
    val pickupBoxesQuantity: String?,
    val pickupColoredBagsQuantity: String?,
    val pickupMailsQuantity: String?,
    val pickupMoneyBagsQuantity: String?,
    val pickupOthersQuantity: String?,
    val pickupNotes: String?,
    val pickupAdditionalNotes: String?,
    val pickupPrintSignatureOne: String?,
    val pickupPrintSignatureTwo: String?,
    val pickupSignatureOne: ByteArray?, // Stored as BLOB
    val pickupSignatureTwo: ByteArray?, // Stored as BLOB

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
    val dropoffBoxesQuantity: String?,
    val dropoffColoredBagsQuantity: String?,
    val dropoffMailsQuantity: String?,
    val dropoffMoneyBagsQuantity: String?,
    val dropoffOthersQuantity: String?,
    val dropoffNotes: String?,
    val dropoffAdditionalNotes: String?,
    val dropoffPrintSignatureOne: String?,
    val dropoffPrintSignatureTwo: String?,
    val dropoffSignatureOne: ByteArray?, // Stored as BLOB
    val dropoffSignatureTwo: ByteArray?, // Stored as BLOB
) {
    // Overriding equals and hashCode to ensure structural comparison of ByteArray properties.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormEntry

        if (id != other.id) return false
        if (entryTitle != other.entryTitle) return false
        if (pickupRun != other.pickupRun) return false
        if (pickupDate != other.pickupDate) return false
        if (pickupDriverName != other.pickupDriverName) return false
        if (pickupDriverNumber != other.pickupDriverNumber) return false
        if (pickupFacilityName != other.pickupFacilityName) return false
        if (pickupFrozenBags != other.pickupFrozenBags) return false
        if (pickupFrozenQuantity != other.pickupFrozenQuantity) return false
        if (pickupRefrigeratedBags != other.pickupRefrigeratedBags) return false
        if (pickupRefrigeratedQuantity != other.pickupRefrigeratedQuantity) return false
        if (pickupRoomTempBags != other.pickupRoomTempBags) return false
        if (pickupRoomTempQuantity != other.pickupRoomTempQuantity) return false
        if (pickupBoxesQuantity != other.pickupBoxesQuantity) return false
        if (pickupColoredBagsQuantity != other.pickupColoredBagsQuantity) return false
        if (pickupMailsQuantity != other.pickupMailsQuantity) return false
        if (pickupMoneyBagsQuantity != other.pickupMoneyBagsQuantity) return false
        if (pickupOthersQuantity != other.pickupOthersQuantity) return false
        if (pickupNotes != other.pickupNotes) return false
        if (pickupAdditionalNotes != other.pickupAdditionalNotes) return false
        if (pickupPrintSignatureOne != other.pickupPrintSignatureOne) return false
        if (pickupPrintSignatureTwo != other.pickupPrintSignatureTwo) return false
        if (pickupSignatureOne != null) {
            if (other.pickupSignatureOne == null) return false
            if (!pickupSignatureOne.contentEquals(other.pickupSignatureOne)) return false
        } else if (other.pickupSignatureOne != null) return false
        if (pickupSignatureTwo != null) {
            if (other.pickupSignatureTwo == null) return false
            if (!pickupSignatureTwo.contentEquals(other.pickupSignatureTwo)) return false
        } else if (other.pickupSignatureTwo != null) return false
        if (dropoffRun != other.dropoffRun) return false
        if (dropoffDate != other.dropoffDate) return false
        if (dropoffDriverName != other.dropoffDriverName) return false
        if (dropoffDriverNumber != other.dropoffDriverNumber) return false
        if (dropoffFacilityName != other.dropoffFacilityName) return false
        if (dropoffFrozenBags != other.dropoffFrozenBags) return false
        if (dropoffFrozenQuantity != other.dropoffFrozenQuantity) return false
        if (dropoffRefrigeratedBags != other.dropoffRefrigeratedBags) return false
        if (dropoffRefrigeratedQuantity != other.dropoffRefrigeratedQuantity) return false
        if (dropoffRoomTempBags != other.dropoffRoomTempBags) return false
        if (dropoffRoomTempQuantity != other.dropoffRoomTempQuantity) return false
        if (dropoffBoxesQuantity != other.dropoffBoxesQuantity) return false
        if (dropoffColoredBagsQuantity != other.dropoffColoredBagsQuantity) return false
        if (dropoffMailsQuantity != other.dropoffMailsQuantity) return false
        if (dropoffMoneyBagsQuantity != other.dropoffMoneyBagsQuantity) return false
        if (dropoffOthersQuantity != other.dropoffOthersQuantity) return false
        if (dropoffNotes != other.dropoffNotes) return false
        if (dropoffAdditionalNotes != other.dropoffAdditionalNotes) return false
        if (dropoffPrintSignatureOne != other.dropoffPrintSignatureOne) return false
        if (dropoffPrintSignatureTwo != other.dropoffPrintSignatureTwo) return false
        if (dropoffSignatureOne != null) {
            if (other.dropoffSignatureOne == null) return false
            if (!dropoffSignatureOne.contentEquals(other.dropoffSignatureOne)) return false
        } else if (other.dropoffSignatureOne != null) return false
        if (dropoffSignatureTwo != null) {
            if (other.dropoffSignatureTwo == null) return false
            if (!dropoffSignatureTwo.contentEquals(other.dropoffSignatureTwo)) return false
        } else if (other.dropoffSignatureTwo != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + entryTitle.hashCode()
        result = 31 * result + (pickupRun?.hashCode() ?: 0)
        result = 31 * result + (pickupDate?.hashCode() ?: 0)
        result = 31 * result + (pickupDriverName?.hashCode() ?: 0)
        result = 31 * result + (pickupDriverNumber?.hashCode() ?: 0)
        result = 31 * result + (pickupFacilityName?.hashCode() ?: 0)
        result = 31 * result + (pickupFrozenBags?.hashCode() ?: 0)
        result = 31 * result + (pickupFrozenQuantity?.hashCode() ?: 0)
        result = 31 * result + (pickupRefrigeratedBags?.hashCode() ?: 0)
        result = 31 * result + (pickupRefrigeratedQuantity?.hashCode() ?: 0)
        result = 31 * result + (pickupRoomTempBags?.hashCode() ?: 0)
        result = 31 * result + (pickupRoomTempQuantity?.hashCode() ?: 0)
        result = 31 * result + (pickupBoxesQuantity?.hashCode() ?: 0)
        result = 31 * result + (pickupColoredBagsQuantity?.hashCode() ?: 0)
        result = 31 * result + (pickupMailsQuantity?.hashCode() ?: 0)
        result = 31 * result + (pickupMoneyBagsQuantity?.hashCode() ?: 0)
        result = 31 * result + (pickupOthersQuantity?.hashCode() ?: 0)
        result = 31 * result + (pickupNotes?.hashCode() ?: 0)
        result = 31 * result + (pickupAdditionalNotes?.hashCode() ?: 0)
        result = 31 * result + (pickupPrintSignatureOne?.hashCode() ?: 0)
        result = 31 * result + (pickupPrintSignatureTwo?.hashCode() ?: 0)
        result = 31 * result + (pickupSignatureOne?.contentHashCode() ?: 0)
        result = 31 * result + (pickupSignatureTwo?.contentHashCode() ?: 0)
        result = 31 * result + (dropoffRun?.hashCode() ?: 0)
        result = 31 * result + (dropoffDate?.hashCode() ?: 0)
        result = 31 * result + (dropoffDriverName?.hashCode() ?: 0)
        result = 31 * result + (dropoffDriverNumber?.hashCode() ?: 0)
        result = 31 * result + (dropoffFacilityName?.hashCode() ?: 0)
        result = 31 * result + (dropoffFrozenBags?.hashCode() ?: 0)
        result = 31 * result + (dropoffFrozenQuantity?.hashCode() ?: 0)
        result = 31 * result + (dropoffRefrigeratedBags?.hashCode() ?: 0)
        result = 31 * result + (dropoffRefrigeratedQuantity?.hashCode() ?: 0)
        result = 31 * result + (dropoffRoomTempBags?.hashCode() ?: 0)
        result = 31 * result + (dropoffRoomTempQuantity?.hashCode() ?: 0)
        result = 31 * result + (dropoffBoxesQuantity?.hashCode() ?: 0)
        result = 31 * result + (dropoffColoredBagsQuantity?.hashCode() ?: 0)
        result = 31 * result + (dropoffMailsQuantity?.hashCode() ?: 0)
        result = 31 * result + (dropoffMoneyBagsQuantity?.hashCode() ?: 0)
        result = 31 * result + (dropoffOthersQuantity?.hashCode() ?: 0)
        result = 31 * result + (dropoffNotes?.hashCode() ?: 0)
        result = 31 * result + (dropoffAdditionalNotes?.hashCode() ?: 0)
        result = 31 * result + (dropoffPrintSignatureOne?.hashCode() ?: 0)
        result = 31 * result + (dropoffPrintSignatureTwo?.hashCode() ?: 0)
        result = 31 * result + (dropoffSignatureOne?.contentHashCode() ?: 0)
        result = 31 * result + (dropoffSignatureTwo?.contentHashCode() ?: 0)
        return result
    }
}