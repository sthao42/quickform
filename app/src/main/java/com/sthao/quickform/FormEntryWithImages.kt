package com.sthao.quickform

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A data class that holds the relationship between a parent [FormEntry]
 * and its list of child [FormImage] objects. Room uses this to combine
 * queries from the two tables into a single, structured object.
 */
data class FormEntryWithImages(
    @Embedded
    val formEntry: FormEntry,

    @Relation(
        parentColumn = "id",
        entityColumn = "formEntryId"
    )
    val images: List<FormImage>
)