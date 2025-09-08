package com.sthao.quickform

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A data class that combines a FormEntry with its associated images and stations item sections
 * for convenient retrieval from the database.
 */
data class FormEntryWithImagesAndSections(
    @Embedded val formEntry: FormEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "formEntryId"
    )
    val images: List<FormImage>,
    @Relation(
        parentColumn = "id",
        entityColumn = "formEntryId"
    )
    val stationsItemSections: List<StationsItemSectionEntity>
)