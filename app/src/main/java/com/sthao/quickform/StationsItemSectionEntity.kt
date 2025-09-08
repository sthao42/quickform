package com.sthao.quickform

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single item section for the Stations screen in the database.
 */
@Entity(
    tableName = "stations_item_sections",
    foreignKeys = [
        ForeignKey(
            entity = FormEntry::class,
            parentColumns = ["id"],
            childColumns = ["formEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["formEntryId"])
    ]
)
data class StationsItemSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val formEntryId: Long = 0,
    val sectionIndex: Int = 0,
    val sectionRunNumber: String = "",
    val totes: String = "",
    val addOns: String = "",
    val extra: String = "",
    val printName: String = "",
    val signature: ByteArray? = null // Stored as BLOB
) {
    // Overriding equals and hashCode to ensure structural comparison of ByteArray properties.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StationsItemSectionEntity

        if (id != other.id) return false
        if (formEntryId != other.formEntryId) return false
        if (sectionIndex != other.sectionIndex) return false
        if (sectionRunNumber != other.sectionRunNumber) return false
        if (totes != other.totes) return false
        if (addOns != other.addOns) return false
        if (extra != other.extra) return false
        if (printName != other.printName) return false
        return signature.contentEquals(other.signature)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + formEntryId.hashCode()
        result = 31 * result + sectionIndex
        result = 31 * result + sectionRunNumber.hashCode()
        result = 31 * result + totes.hashCode()
        result = 31 * result + addOns.hashCode()
        result = 31 * result + extra.hashCode()
        result = 31 * result + printName.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}