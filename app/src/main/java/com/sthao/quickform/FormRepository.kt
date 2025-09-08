package com.sthao.quickform

import kotlinx.coroutines.flow.Flow

/**
 * A repository class that abstracts access to the FormDao.
 * This provides a clean API for the ViewModel to interact with the data layer.
 */
class FormRepository(private val formDao: FormDao) {

    // Provides a flow of lightweight list items for the saved forms screen.
    val allFormListItems: Flow<List<FormListItem>> = formDao.getAllListItems()

    // Gets a single, complete form entry with all its images and stations item sections by its ID.
    fun getFormWithImagesAndSectionsById(id: Long): Flow<FormEntryWithImagesAndSections> {
        return formDao.getFormWithImagesAndSectionsById(id)
    }

    // Gets a list of complete form entries with their images from a list of IDs.
    suspend fun getFormsWithImagesByIds(ids: List<Long>): List<FormEntryWithImagesAndSections> {
        return formDao.getFormsWithImagesByIds(ids)
    }

    // Gets a list of complete form entries with their images and sections from a list of IDs.
    suspend fun getFormsWithImagesAndSectionsByIds(ids: List<Long>): List<FormEntryWithImagesAndSections> {
        return formDao.getFormsWithImagesAndSectionsByIds(ids)
    }

    // Saves a form entry and its associated images in a single transaction.
    suspend fun saveFormWithImages(formEntry: FormEntry, images: List<FormImage>) {
        formDao.saveFormWithImages(formEntry, images)
    }

    // Saves a form entry, its associated images, and stations item sections in a single transaction.
    suspend fun saveFormWithImagesAndSections(formEntry: FormEntry, images: List<FormImage>, sections: List<StationsItemSectionEntity>) {
        formDao.saveFormWithImagesAndSections(formEntry, images, sections)
    }

    // Deletes a list of forms from the database by their IDs.
    suspend fun deleteByIds(ids: List<Long>) {
        formDao.deleteByIds(ids)
    }

    // Gets all forms that were created on a specific date.
    fun getFormsByDate(date: String): Flow<List<FormEntry>> {
        return formDao.getFormsByDate(date)
    }
}