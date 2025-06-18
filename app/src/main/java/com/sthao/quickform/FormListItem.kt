package com.sthao.quickform

/**
 * A lightweight Data Transfer Object (DTO) representing a single item
 * in the Saved Entries list. It only contains the fields necessary for display,
 * avoiding loading heavy image/signature data into memory for the entire list.
 */
data class FormListItem(
    val id: Long,
    val entryTitle: String,
    val pickupRun: String,
    val pickupFacilityName: String
)