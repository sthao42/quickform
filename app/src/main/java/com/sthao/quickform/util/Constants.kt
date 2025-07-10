package com.sthao.quickform.util

/**
 * Application-wide constants to avoid magic numbers and strings.
 */
object Constants {
    
    // Database constants
    const val DATABASE_NAME = "form_database"
    const val DATABASE_VERSION = 7
    
    // Image processing constants
    const val IMAGE_MAX_DIMENSION = 1024
    const val IMAGE_QUALITY_JPEG = 85
    const val IMAGE_QUALITY_PNG = 100
    
    // File provider constants
    const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"
    const val CACHE_IMAGES_DIR = "images"
    const val SHARED_PDFS_DIR = "shared_pdfs"
    
    // Form constants
    const val DATE_FORMAT_DISPLAY = "MMM-dd-yyyy"
    const val DATE_FORMAT_FILENAME = "yyyyMMdd_HHmmss"
    const val FORM_TITLE_ID_PADDING = 3
    
    // PDF constants
    const val PDF_PAGE_WIDTH = 612
    const val PDF_PAGE_HEIGHT = 792
    const val PDF_MARGIN = 50f
    const val PDF_LINE_SPACING = 20f
    const val PDF_SECTION_SPACING = 35f
    const val PDF_IMAGE_MAX_WIDTH = PDF_PAGE_WIDTH - (PDF_MARGIN * 2)
    const val PDF_IMAGE_MAX_HEIGHT = 400f
    
    // UI constants
    const val SIGNATURE_BOX_HEIGHT_DP = 180
    const val IMAGE_PREVIEW_SIZE_DP = 100
    const val DIALOG_CANVAS_HEIGHT_DP = 200
    const val LOGO_HEIGHT_DP = 30
    const val FAB_SIZE_DP = 64
    
    // StateFlow configuration
    const val STATEFLOW_SUBSCRIPTION_TIMEOUT = 5000L
    
    // Form validation
    const val MIN_FACILITY_NAME_LENGTH = 1
    const val MAX_NOTES_LENGTH = 1000
    
    // Toast messages
    const val TOAST_ENTRY_SAVED = "Entry Saved!"
    const val TOAST_FACILITY_NAME_EMPTY = "Cannot save, facility name is empty."
    const val TOAST_PDF_SAVED = "PDF saved to Downloads"
    const val TOAST_NO_ENTRIES_SELECTED = "No entries selected for export."
    const val TOAST_ERROR_EXPORTING = "Error exporting PDF"
    
    // Image types
    const val IMAGE_TYPE_PICKUP = "PICKUP"
    const val IMAGE_TYPE_DROPOFF = "DROPOFF"
    
    // File extensions
    const val PNG_EXTENSION = ".png"
    const val PDF_EXTENSION = ".pdf"
    
    // Form field defaults
    const val DEFAULT_QUANTITY = "0"
    const val DEFAULT_EMPTY_VALUE = "N/A"
}
