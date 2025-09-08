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
 * @param sectionIndex The index of the section this image belongs to (for Stations item sections).
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
    val imageData: ByteArray,
    val sectionIndex: Int = -1 // -1 for non-section images, >= 0 for section images
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
        if (sectionIndex != other.sectionIndex) return false
        return imageData.contentEquals(other.imageData)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + formEntryId.hashCode()
        result = 31 * result + imageType.hashCode()
        result = 31 * result + sectionIndex
        result = 31 * result + imageData.contentHashCode()
        return result
    }
}