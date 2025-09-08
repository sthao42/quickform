package com.sthao.quickform.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.sthao.quickform.FormEntryWithImagesAndSections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Exports a list of FormEntry objects to a single PDF file and initiates a share intent.
 *
 * @param context The context needed to create intents and access file providers.
 * @param forms The list of FormEntry objects to include in the PDF.
 * @param scope The CoroutineScope to launch the operation in, preventing UI blocking.
 */
fun shareFormsAsPdf(
    context: Context,
    forms: List<FormEntryWithImagesAndSections>,
    scope: CoroutineScope
) {
    if (forms.isEmpty()) {
        Toast.makeText(context, "No forms selected to share.", Toast.LENGTH_SHORT).show()
        return
    }

    // Launch a coroutine to handle PDF creation and sharing off the main thread.
    scope.launch(Dispatchers.IO) {
        // Step 1: Export the selected forms to a PDF file in the app's cache.
        val pdfFile: File? = exportToPdfForSharing(context, forms)

        // Switch back to the main thread to show toasts or start activities.
        withContext(Dispatchers.Main) {
            if (pdfFile != null && pdfFile.exists()) {
                // Step 2: Get a content URI for the PDF file via a FileProvider.
                // This is the modern, secure way to grant other apps access to your app's files.
                val pdfUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider", // Authority must match AndroidManifest.xml
                    pdfFile
                )

                // Step 3: Create an ACTION_SEND intent to share the PDF.
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                    // Grant temporary read permission to the app that receives the intent.
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    // Optional: Add a subject and text for email clients.
                    val formCount = forms.size
                    val subjectText = if (formCount == 1) "1 Form Entry" else "$formCount Form Entries"
                    putExtra(Intent.EXTRA_SUBJECT, "QuickForm Export: $subjectText")
                    putExtra(Intent.EXTRA_TEXT, "Attached are the exported form entries in PDF format.")
                }

                // Step 4: Create a chooser to let the user pick an app and then launch it.
                val chooserIntent = Intent.createChooser(shareIntent, "Share PDF via...")
                context.startActivity(chooserIntent)
            } else {
                // Handle the case where PDF creation failed.
                Toast.makeText(context, "Failed to create PDF for sharing.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
