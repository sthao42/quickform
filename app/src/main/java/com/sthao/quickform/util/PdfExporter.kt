package com.sthao.quickform.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.sthao.quickform.FormEntry
import com.sthao.quickform.FormEntryWithImages
import com.sthao.quickform.FormImage
import com.sthao.quickform.util.Constants.DATE_FORMAT_FILENAME
import com.sthao.quickform.util.Constants.DEFAULT_EMPTY_VALUE
import com.sthao.quickform.util.Constants.DEFAULT_QUANTITY
import com.sthao.quickform.util.Constants.IMAGE_TYPE_DROPOFF
import com.sthao.quickform.util.Constants.IMAGE_TYPE_PICKUP
import com.sthao.quickform.util.Constants.PDF_IMAGE_MAX_HEIGHT
import com.sthao.quickform.util.Constants.PDF_IMAGE_MAX_WIDTH
import com.sthao.quickform.util.Constants.PDF_LINE_SPACING
import com.sthao.quickform.util.Constants.PDF_MARGIN
import com.sthao.quickform.util.Constants.PDF_PAGE_HEIGHT
import com.sthao.quickform.util.Constants.PDF_PAGE_WIDTH
import com.sthao.quickform.util.Constants.PDF_SECTION_SPACING
import com.sthao.quickform.util.Constants.SHARED_PDFS_DIR
import com.sthao.quickform.util.Constants.TOAST_ERROR_EXPORTING
import com.sthao.quickform.util.Constants.TOAST_NO_ENTRIES_SELECTED
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

// Defines common dimensions and styling for the PDF layout.
private object PdfDimens {
    const val PAGE_WIDTH = PDF_PAGE_WIDTH
    const val PAGE_HEIGHT = PDF_PAGE_HEIGHT
    const val MARGIN = PDF_MARGIN
    const val LINE_SPACING = PDF_LINE_SPACING
    const val SECTION_SPACING = PDF_SECTION_SPACING
    const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    const val IMAGE_MAX_WIDTH = PDF_IMAGE_MAX_WIDTH
    const val IMAGE_MAX_HEIGHT = PDF_IMAGE_MAX_HEIGHT


    val TITLE_PAINT = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 18f; color = Color.BLACK; textAlign = Paint.Align.CENTER }
    val SECTION_TITLE_PAINT = Paint().apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textSize = 14f; color = Color.BLACK }
    val HEADER_PAINT = Paint().apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textSize = 11f; color = Color.BLACK }
    val BODY_PAINT = Paint().apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textSize = 11f; color = Color.DKGRAY }
    val MONO_BODY_PAINT = Paint(BODY_PAINT).apply { typeface = Typeface.MONOSPACE }
}

/**
 * Exports a list of forms to a single PDF file in the app's cache for sharing.
 */
fun exportToPdfForSharing(context: Context, forms: List<FormEntryWithImages>): File? {
    if (forms.isEmpty()) return null

    val pdfDocument = PdfDocument()
    return try {
        val formsWithIds = forms.mapIndexed { index, form -> form to (index + 1).toString() }
        formsWithIds.forEach { (formWithImages, sequentialId) ->
            drawFormOnPdf(pdfDocument, formWithImages, sequentialId)
        }

        val timestamp = SimpleDateFormat(DATE_FORMAT_FILENAME, Locale.US).format(Date())
        val fileName = "QuickForm_Export_${timestamp}.pdf"
        val outputDir = File(context.cacheDir, SHARED_PDFS_DIR).apply { mkdirs() }
        val outputFile = File(outputDir, fileName)

        outputFile.outputStream().use { pdfDocument.writeTo(it) }
        outputFile
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } finally {
        pdfDocument.close()
    }
}

/**
 * Exports multiple forms to a single PDF file and saves it to the device's Downloads folder.
 */
fun exportMultipleFormsAsPdf(context: Context, formsWithIds: List<Pair<FormEntryWithImages, String>>) {
    if (formsWithIds.isEmpty()) {
        Toast.makeText(context, TOAST_NO_ENTRIES_SELECTED, Toast.LENGTH_SHORT).show()
        return
    }

    val pdfDocument = PdfDocument()
    try {
        formsWithIds.forEach { (formWithImages, sequentialId) ->
            drawFormOnPdf(pdfDocument, formWithImages, sequentialId)
        }

        val timestamp = SimpleDateFormat(DATE_FORMAT_FILENAME, Locale.US).format(Date())
        val fileName = "Exported_Forms_${timestamp}.pdf"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
                Toast.makeText(context, "PDF with ${formsWithIds.size} entries saved to Downloads", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "$TOAST_ERROR_EXPORTING: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}

/**
 * A helper class to manage page creation and Y-coordinate positioning within a PdfDocument.
 */
private class PdfLayoutManager(private val document: PdfDocument) {
    private var currentPage: PdfDocument.Page? = null
    private var canvas: Canvas? = null
    var yPos = 0f
        private set

    init {
        startNewPage()
    }

    fun startNewPage() {
        currentPage?.let { document.finishPage(it) }
        val pageInfo = PdfDocument.PageInfo.Builder(PdfDimens.PAGE_WIDTH, PdfDimens.PAGE_HEIGHT, document.pages.size + 1).create()
        val newPage = document.startPage(pageInfo)
        currentPage = newPage
        canvas = newPage.canvas
        yPos = PdfDimens.MARGIN
    }

    fun finishPage() {
        currentPage?.let { document.finishPage(it); currentPage = null }
    }

    fun prepareToDraw(spaceNeeded: Float) {
        if (yPos + spaceNeeded > PdfDimens.PAGE_HEIGHT - PdfDimens.MARGIN) {
            startNewPage()
        }
    }

    fun advanceY(space: Float) {
        yPos += space
    }

    fun draw(drawCommand: (Canvas) -> Unit) {
        canvas?.let(drawCommand)
    }
}

/**
 * Main orchestrator function that draws a full form (Pickup and Dropoff sections) onto a PDF.
 */
private fun drawFormOnPdf(document: PdfDocument, formWithImages: FormEntryWithImages, sequentialId: String) {
    val formEntry = formWithImages.formEntry
    val pickupImages = formWithImages.images.filter { it.imageType == IMAGE_TYPE_PICKUP }
    val dropoffImages = formWithImages.images.filter { it.imageType == IMAGE_TYPE_DROPOFF }

    // Draws the Pickup section on its own page.
    val pickupLayoutManager = PdfLayoutManager(document)
    drawPageHeader(pickupLayoutManager, "Form Entry: ${formEntry.pickupDate ?: "N/A"}-$sequentialId (Pickup)")
    drawSectionContent(pickupLayoutManager, "Pickup Information", formEntry, pickupImages, isPickup = true)
    pickupLayoutManager.finishPage()

    // Draws the Dropoff section on its own page.
    val dropoffLayoutManager = PdfLayoutManager(document)
    drawPageHeader(dropoffLayoutManager, "Form Entry: ${formEntry.dropoffDate ?: "N/A"}-$sequentialId (Drop-off)")
    drawSectionContent(dropoffLayoutManager, "Drop-off Information", formEntry, dropoffImages, isPickup = false)
    dropoffLayoutManager.finishPage()
}

/**
 * Draws all content for a given section, matching the new UI layout.
 */
private fun drawSectionContent(layoutManager: PdfLayoutManager, title: String, form: FormEntry, images: List<FormImage>, isPickup: Boolean) {
    drawSubHeader(layoutManager, title)
    drawTwoColumnInfo(layoutManager, form, isPickup)
    drawItemDetails(layoutManager, form, isPickup)
    drawSignature(layoutManager, form, isPickup, signatureIndex = 1)
    drawMiscItemDetails(layoutManager, form, isPickup)
    // The second signature is now drawn before the attached images.
    drawSignature(layoutManager, form, isPickup, signatureIndex = 2)
    drawAttachedImages(layoutManager, images)
}

/**
 * Draws a centered header at the top of the PDF page.
 */
private fun drawPageHeader(layoutManager: PdfLayoutManager, title: String) {
    layoutManager.prepareToDraw(PdfDimens.SECTION_SPACING * 1.5f)
    layoutManager.draw { canvas ->
        canvas.drawText(title, (PdfDimens.MARGIN + PdfDimens.CONTENT_WIDTH + PdfDimens.MARGIN) / 2, layoutManager.yPos, PdfDimens.TITLE_PAINT)
    }
    layoutManager.advanceY(PdfDimens.SECTION_SPACING * 1.5f)
}

/**
 * Draws a left-aligned sub-header for a content section (e.g., "Pickup Information").
 */
private fun drawSubHeader(layoutManager: PdfLayoutManager, title: String) {
    layoutManager.prepareToDraw(PdfDimens.LINE_SPACING * 1.5f)
    layoutManager.draw { canvas ->
        canvas.drawText(title, PdfDimens.MARGIN, layoutManager.yPos, PdfDimens.SECTION_TITLE_PAINT)
    }
    layoutManager.advanceY(PdfDimens.LINE_SPACING * 1.5f)
}


/**
 * Draws the basic form info (Facility, Driver, Date, Run#) in a two-column layout.
 */
private fun drawTwoColumnInfo(layoutManager: PdfLayoutManager, form: FormEntry, isPickup: Boolean) {
    layoutManager.prepareToDraw(PdfDimens.LINE_SPACING * 2)
    val startYForInfo = layoutManager.yPos
    layoutManager.draw { canvas ->
        val facility = if (isPickup) form.pickupFacilityName else form.dropoffFacilityName
        val driverName = if (isPickup) form.pickupDriverName else form.dropoffDriverName
        val driverNum = if (isPickup) form.pickupDriverNumber else form.dropoffDriverNumber
        val date = if (isPickup) form.pickupDate else form.dropoffDate
        val run = if (isPickup) form.pickupRun else form.dropoffRun

        canvas.drawText("Facility:", PdfDimens.MARGIN, layoutManager.yPos, PdfDimens.HEADER_PAINT)
        canvas.drawText((facility ?: "").ifEmpty { DEFAULT_EMPTY_VALUE }, PdfDimens.MARGIN + 60, layoutManager.yPos, PdfDimens.BODY_PAINT)
        canvas.drawText("Driver:", PdfDimens.MARGIN, layoutManager.yPos + PdfDimens.LINE_SPACING, PdfDimens.HEADER_PAINT)
        canvas.drawText("${driverName ?: ""} (#${driverNum ?: ""})", PdfDimens.MARGIN + 60, layoutManager.yPos + PdfDimens.LINE_SPACING, PdfDimens.BODY_PAINT)

        val col2X = PdfDimens.MARGIN + PdfDimens.CONTENT_WIDTH / 2
        canvas.drawText("Date:", col2X, startYForInfo, PdfDimens.HEADER_PAINT)
        canvas.drawText((date ?: "").ifEmpty { DEFAULT_EMPTY_VALUE }, col2X + 50, startYForInfo, PdfDimens.BODY_PAINT)
        canvas.drawText("Run #:", col2X, startYForInfo + PdfDimens.LINE_SPACING, PdfDimens.HEADER_PAINT)
        canvas.drawText((run ?: "").ifEmpty { DEFAULT_EMPTY_VALUE }, col2X + 50, startYForInfo + PdfDimens.LINE_SPACING, PdfDimens.BODY_PAINT)
    }
    layoutManager.advanceY(PdfDimens.LINE_SPACING * 2 + PdfDimens.SECTION_SPACING)
}

/**
 * Draws the table of primary item quantities.
 */
private fun drawItemDetails(layoutManager: PdfLayoutManager, form: FormEntry, isPickup: Boolean) {
    layoutManager.prepareToDraw(PdfDimens.LINE_SPACING)
    layoutManager.draw { canvas -> canvas.drawText("Item Details", PdfDimens.MARGIN, layoutManager.yPos, PdfDimens.HEADER_PAINT) }
    layoutManager.advanceY(PdfDimens.LINE_SPACING)

    val itemsData = if (isPickup) {
        listOf("Frozen" to (form.pickupFrozenBags to form.pickupFrozenQuantity), "Refrigerated" to (form.pickupRefrigeratedBags to form.pickupRefrigeratedQuantity), "Room Temp" to (form.pickupRoomTempBags to form.pickupRoomTempQuantity))
    } else {
        listOf("Frozen" to (form.dropoffFrozenBags to form.dropoffFrozenQuantity), "Refrigerated" to (form.dropoffRefrigeratedBags to form.dropoffRefrigeratedQuantity), "Room Temp" to (form.dropoffRoomTempBags to form.dropoffRoomTempQuantity))
    }
    itemsData.forEach { (name, pair) ->
        layoutManager.prepareToDraw(PdfDimens.LINE_SPACING)
        layoutManager.draw { canvas ->
            val text = "${name.padEnd(15, ' ')} Bags: ${(pair.first ?: "").ifBlank { DEFAULT_QUANTITY }.padStart(3)} | Quantity: ${(pair.second ?: "").ifBlank { DEFAULT_QUANTITY }.padStart(3)}"
            canvas.drawText(text, PdfDimens.MARGIN + 10, layoutManager.yPos, PdfDimens.MONO_BODY_PAINT)
        }
        layoutManager.advanceY(PdfDimens.LINE_SPACING)
    }
    layoutManager.advanceY(PdfDimens.SECTION_SPACING)
}

/**
 * Draws the miscellaneous item quantities, each on its own line with aligned values.
 */
private fun drawMiscItemDetails(layoutManager: PdfLayoutManager, form: FormEntry, isPickup: Boolean) {
    val miscItems = if (isPickup) {
        listOf(
            "Boxes" to form.pickupBoxesQuantity,
            "Colored Bags" to form.pickupColoredBagsQuantity,
            "Mails" to form.pickupMailsQuantity,
            "Money Bags" to form.pickupMoneyBagsQuantity,
            "Others" to form.pickupOthersQuantity
        )
    } else {
        listOf(
            "Boxes" to form.dropoffBoxesQuantity,
            "Colored Bags" to form.dropoffColoredBagsQuantity,
            "Mails" to form.dropoffMailsQuantity,
            "Money Bags" to form.dropoffMoneyBagsQuantity,
            "Others" to form.dropoffOthersQuantity
        )
    }

    // Values are now drawn at a fixed X-position for vertical alignment.
    val valueXPos = PdfDimens.MARGIN + 100f
    miscItems.forEach { (label, value) ->
        layoutManager.prepareToDraw(PdfDimens.LINE_SPACING)
        layoutManager.draw { canvas ->
            // Draw label
            canvas.drawText("${label}:", PdfDimens.MARGIN + 10, layoutManager.yPos, PdfDimens.BODY_PAINT)
            // Draw value at the aligned position
            canvas.drawText((value ?: "").ifBlank { DEFAULT_QUANTITY }, valueXPos, layoutManager.yPos, PdfDimens.BODY_PAINT)
        }
        layoutManager.advanceY(PdfDimens.LINE_SPACING)
    }
    layoutManager.advanceY(PdfDimens.SECTION_SPACING)
}


/**
 * Draws the printed name and signature bitmap onto the PDF.
 * @param signatureIndex Can be 1 or 2 to specify which signature to draw.
 */
private fun drawSignature(layoutManager: PdfLayoutManager, form: FormEntry, isPickup: Boolean, signatureIndex: Int) {
    val signatureByteArray: ByteArray?
    val printedName: String?
    val label: String

    if (signatureIndex == 1) {
        signatureByteArray = if (isPickup) form.pickupSignatureOne else form.dropoffSignatureOne
        printedName = if (isPickup) form.pickupPrintSignatureOne else form.dropoffPrintSignatureOne
        label = "Signature #1"
    } else { // signatureIndex == 2
        signatureByteArray = if (isPickup) form.pickupSignatureTwo else form.dropoffSignatureTwo
        printedName = if (isPickup) form.pickupPrintSignatureTwo else form.dropoffPrintSignatureTwo
        label = "Signature #2"
    }

    val signatureBoxWidth = PdfDimens.CONTENT_WIDTH / 2.5f
    val signatureBoxHeight = 60f

    layoutManager.prepareToDraw(PdfDimens.LINE_SPACING + signatureBoxHeight + (PdfDimens.LINE_SPACING * 2))
    layoutManager.draw { it.drawText("Print Name:", PdfDimens.MARGIN, layoutManager.yPos, PdfDimens.HEADER_PAINT) }
    layoutManager.draw { it.drawText((printedName ?: "").ifEmpty { DEFAULT_EMPTY_VALUE }, PdfDimens.MARGIN + 80, layoutManager.yPos, PdfDimens.BODY_PAINT) }
    layoutManager.advanceY(PdfDimens.LINE_SPACING)

    val signatureRect = RectF(PdfDimens.MARGIN, layoutManager.yPos, PdfDimens.MARGIN + signatureBoxWidth, layoutManager.yPos + signatureBoxHeight)
    byteArrayToBitmap(signatureByteArray ?: byteArrayOf())?.let { signatureBitmap ->
        layoutManager.draw { it.drawBitmap(signatureBitmap, null, signatureRect, null) }
    }
    layoutManager.advanceY(signatureBoxHeight)

    layoutManager.draw{ canvas ->
        canvas.drawLine(PdfDimens.MARGIN, layoutManager.yPos, PdfDimens.MARGIN + signatureBoxWidth, layoutManager.yPos, PdfDimens.BODY_PAINT)
        canvas.drawText(label, PdfDimens.MARGIN, layoutManager.yPos + PdfDimens.LINE_SPACING, PdfDimens.HEADER_PAINT)
    }
    layoutManager.advanceY(PdfDimens.LINE_SPACING * 2)
}

/**
 * Draws all attached images in a single column, scaled to fit the page width.
 */
private fun drawAttachedImages(layoutManager: PdfLayoutManager, images: List<FormImage>) {
    layoutManager.prepareToDraw(PdfDimens.LINE_SPACING)
    layoutManager.draw { canvas ->
        canvas.drawText("Attached Images:", PdfDimens.MARGIN, layoutManager.yPos, PdfDimens.HEADER_PAINT)
        if (images.isEmpty()) {
            canvas.drawText(DEFAULT_EMPTY_VALUE, PdfDimens.MARGIN + 110, layoutManager.yPos, PdfDimens.BODY_PAINT)
        }
    }
    layoutManager.advanceY(PdfDimens.LINE_SPACING)

    if (images.isNotEmpty()) {
        images.forEach { formImage ->
            byteArrayToBitmap(formImage.imageData)?.let { bmp ->
                // Calculate scale to fit width, but don't upscale if image is smaller
                val widthScale = if (bmp.width > PdfDimens.IMAGE_MAX_WIDTH) {
                    PdfDimens.IMAGE_MAX_WIDTH / bmp.width.toFloat()
                } else {
                    1.0f // Don't upscale if image is smaller than max width
                }
                
                // Calculate scale to fit height, but don't upscale if image is smaller
                val heightScale = if (bmp.height > PdfDimens.IMAGE_MAX_HEIGHT) {
                    PdfDimens.IMAGE_MAX_HEIGHT / bmp.height.toFloat()
                } else {
                    1.0f // Don't upscale if image is smaller than max height
                }
                
                // Use the smaller scale to ensure image fits both dimensions
                val scale = min(widthScale, heightScale)

                val scaledWidth = bmp.width * scale
                val scaledHeight = bmp.height * scale

                // Check if we need to start a new page for this image
                if (layoutManager.yPos + scaledHeight > PdfDimens.PAGE_HEIGHT - PdfDimens.MARGIN) {
                    layoutManager.startNewPage()
                }

                val xPos = PdfDimens.MARGIN + (PdfDimens.CONTENT_WIDTH - scaledWidth) / 2
                val destRect = RectF(xPos, layoutManager.yPos, xPos + scaledWidth, layoutManager.yPos + scaledHeight)

                layoutManager.draw { canvas ->
                    canvas.drawBitmap(bmp, null, destRect, null)
                }
                
                // Add spacing between images (using section spacing for better visual separation)
                layoutManager.advanceY(scaledHeight + PdfDimens.SECTION_SPACING / 2)
            }
        }
    }
    layoutManager.advanceY(PdfDimens.SECTION_SPACING)
}
