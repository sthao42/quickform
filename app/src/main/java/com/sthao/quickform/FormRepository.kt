package com.sthao.quickform

import kotlinx.coroutines.flow.Flow

/**
 * A repository class that abstracts access to the FormDao.
 * This provides a clean API for the ViewModel to interact with the data layer.
 */
class FormRepository(private val formDao: FormDao) {

    // Provides a flow of lightweight list items for the saved forms screen.
    val allFormListItems: Flow<List<FormListItem>> = formDao.getAllListItems()

    // Gets a single, complete form entry with all its images by its ID.
    fun getFormWithImagesById(id: Long): Flow<FormEntryWithImages> {
        return formDao.getFormWithImagesById(id)
    }

    // Gets a list of complete form entries with their images from a list of IDs.
    suspend fun getFormsWithImagesByIds(ids: List<Long>): List<FormEntryWithImages> {
        return formDao.getFormsWithImagesByIds(ids)
    }

    // Saves a form entry and its associated images in a single transaction.
    suspend fun saveFormWithImages(formEntry: FormEntry, images: List<FormImage>) {
        formDao.saveFormWithImages(formEntry, images)
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