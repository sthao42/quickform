package com.sthao.quickform

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single image associated with a FormEntry.
 * This creates a new 'form_images' table in the database.
 *
 * @param formEntryId The ID of the FormEntry this image belongs to.
 * @param imageType A string to distinguish between "PICKUP" and "DROPOFF" images.
 * @param imageData The downsampled image data stored as a byte array.
 */
@Entity(
    tableName = "form_images",
    foreignKeys = [ForeignKey(
        entity = FormEntry::class,
        parentColumns = ["id"],
        childColumns = ["formEntryId"],
        onDelete = ForeignKey.CASCADE // If a FormEntry is deleted, its images are also deleted.
    )],
    indices = [Index(value = ["formEntryId"])] // Index for faster queries
)
data class FormImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val formEntryId: Long,
    val imageType: String,
    val imageData: ByteArray
) {
    // Overriding equals and hashCode is necessary for robust data class behavior
    // when a property is a ByteArray.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormImage

        if (id != other.id) return false
        if (formEntryId != other.formEntryId) return false
        if (imageType != other.imageType) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + formEntryId.hashCode()
        result = 31 * result + imageType.hashCode()
        result = 31 * result + imageData.contentHashCode()
        return result
    }
}