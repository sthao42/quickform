package com.sthao.quickform

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {

    // --- Internal methods for use within transactions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFormEntry(formEntry: FormEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImages(images: List<FormImage>)

    @Query("DELETE FROM form_images WHERE formEntryId = :formEntryId")
    suspend fun deleteImagesForForm(formEntryId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStationsItemSections(sections: List<StationsItemSectionEntity>)

    @Query("DELETE FROM stations_item_sections WHERE formEntryId = :formEntryId")
    suspend fun deleteStationsItemSectionsForForm(formEntryId: Long)


    /**
     * Saves a form entry and its images in a single, atomic transaction.
     * This ensures data integrity by preventing partial saves.
     */
    @Transaction
    suspend fun saveFormWithImages(formEntry: FormEntry, images: List<FormImage>) {
        // Upsert the main FormEntry and get its ID.
        val entryId = upsertFormEntry(formEntry)
        // Delete any old images to ensure a clean slate.
        deleteImagesForForm(entryId)
        // Associate the new images with the FormEntry's ID and insert them.
        if (images.isNotEmpty()) {
            val imagesWithId = images.map { it.copy(formEntryId = entryId) }
            upsertImages(imagesWithId)
        }
    }

    /**
     * Saves a form entry, its images, and its stations item sections in a single, atomic transaction.
     * This ensures data integrity by preventing partial saves.
     */
    @Transaction
    suspend fun saveFormWithImagesAndSections(formEntry: FormEntry, images: List<FormImage>, sections: List<StationsItemSectionEntity>) {
        // Upsert the main FormEntry and get its ID.
        val entryId = upsertFormEntry(formEntry)
        // Delete any old images to ensure a clean slate.
        deleteImagesForForm(entryId)
        // Delete any old stations item sections to ensure a clean slate.
        deleteStationsItemSectionsForForm(entryId)
        // Associate the new images with the FormEntry's ID and insert them.
        if (images.isNotEmpty()) {
            val imagesWithId = images.map { it.copy(formEntryId = entryId) }
            upsertImages(imagesWithId)
        }
        // Associate the new sections with the FormEntry's ID and insert them.
        if (sections.isNotEmpty()) {
            val sectionsWithId = sections.map { it.copy(formEntryId = entryId) }
            upsertStationsItemSections(sectionsWithId)
        }
    }


    // --- Queries for retrieving full form data ---
    @Transaction
    @Query("SELECT * FROM form_entries WHERE id = :id")
    fun getFormWithImagesAndSectionsById(id: Long): Flow<FormEntryWithImagesAndSections>

    @Transaction
    @Query("SELECT * FROM form_entries WHERE id IN (:ids)")
    suspend fun getFormsWithImagesByIds(ids: List<Long>): List<FormEntryWithImagesAndSections>

    @Transaction
    @Query("SELECT * FROM form_entries WHERE id IN (:ids)")
    suspend fun getFormsWithImagesAndSectionsByIds(ids: List<Long>): List<FormEntryWithImagesAndSections>


    // --- Queries for lightweight or specific data ---
    /**
     * Provides a lightweight list of forms for fast display in the "Saved Forms" screen.
     */
    @Query("SELECT id, entryTitle, pickupRun, pickupFacilityName FROM form_entries ORDER BY id DESC")
    fun getAllListItems(): Flow<List<FormListItem>>

    /**
     * Deletes a list of forms by their IDs.
     */
    @Query("DELETE FROM form_entries WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    /**
     * Used for generating a unique entry title based on the date.
     */
    @Query("SELECT * FROM form_entries WHERE entryTitle LIKE :date || '%'")
    fun getFormsByDate(date: String): Flow<List<FormEntry>>
}