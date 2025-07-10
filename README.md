# Quick Form Android App

This is a comprehensive data entry application built with modern Android development practices using Kotlin, Jetpack Compose (Material 3), and Room Database. It's designed to be a robust tool for filling out, saving, and managing daily confirmation forms.

## Features

-   **Dynamic Two-Section Form**: A detailed form with identical sections for both "Pick Up" and "Drop Off" operations. Fields include:
    -   Run #, Date, Driver Name, Driver Number, Facility Name, Notes, and a printed name for the signature.
    -   Detailed item tracking for various temperature types and miscellaneous items.

-   **Multi-Image Attachments**:
    -   Attach multiple photos to both the Pick Up and Drop Off sections of a form.
    -   A user-friendly UI displays image thumbnails in a responsive, wrapping grid.
    -   Users can easily add new photos or remove existing ones.
    -   Includes smart image handling:
        -   **Efficient Memory Management**: Large images are automatically downsampled before being saved to the database to prevent crashes and save storage space.
        -   **Automatic Rotation Correction**: Photo orientation is read from EXIF data and automatically corrected so images display properly in the app and in PDF exports.

-   **E-Signature Capture**:
    -   A smooth, canvas-based signature box allows for electronic signature capture.
    -   Signatures are saved with a transparent background and preserved in generated PDFs.

-   **Robust Data Persistence**:
    -   All form entries are saved locally using the Room persistence library.
    -   Features a normalized, relational database schema, with a dedicated table for images to efficiently handle the one-to-many relationship between a form and its photos.
    -   A unique ID is generated for each entry in the format `YEAR-MONTH-DAY-000`, incrementing daily.

-   **Saved Entries Management**:
    -   A dedicated screen lists all saved forms for easy review and access.
    -   A multi-select mode allows for performing bulk operations on saved entries.
    -   A contextual app bar appears in selection mode with options to delete, share, or export.

-   **PDF Exporting and Sharing**:
    -   Select and export multiple entries into a single, cleanly formatted multi-page PDF document.
    -   Share selected entries with other apps (e.g., email, cloud storage) as a PDF file.
    -   PDFs feature a consistent layout, with attached images neatly organized into a column grid.

## Project Structure
-   `MainActivity.kt`: Main activity and host for the Jetpack Compose UI.
-   `ui/`
    -   `saved/`, `pickup/`, `dropoff/`: Composable screens for each section of the app.
    -   `components/`: Reusable UI components like `MultiImagePicker`, `SignatureBox`, and `SelectionAppBar`.
    -   `theme/`: Compose theming (colors, typography).
-   `data/` (Conceptual package for data layer files)
    -   `FormEntry.kt`, FormImage.kt, FormEntryWithImages.kt: Room entity and relation classes.
    -   `FormDao.kt`: Data Access Object for database operations.
    -   `FormDatabase.kt`: Room database definition and migrations.
    -   `FormRepository.kt`: Repository to abstract data sources.
-   `viewmodel/`
    -   `FormViewModel.kt`: The main ViewModel handling business logic and state.
    -   `FormEvent.kt`: Defines all user interactions.
-   `util/`
    -   `PdfExporter.kt`: Logic for creating and saving PDF documents.
    -   `ConversionUtils.kt`: Helper functions for image manipulation (downsampling, rotation, etc.).

## How to Build and Run
1.  Open the project in a recent version of Android Studio.
2.  Build the project using the Gradle task or by clicking the "Run" button.
3.  Run on an Android 10+ emulator or physical device.

## Requirements
-   JDK 21+
-   Android SDK 29+ (target 36)