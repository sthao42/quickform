package com.sthao.quickform.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.sthao.quickform.util.Constants.CACHE_IMAGES_DIR
import com.sthao.quickform.util.Constants.FILE_PROVIDER_AUTHORITY_SUFFIX
import com.sthao.quickform.util.Constants.IMAGE_QUALITY_JPEG
import com.sthao.quickform.util.Constants.IMAGE_QUALITY_PNG
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/**
 * Takes a Uri, downsamples the image to a max dimension, and returns it as a JPEG byte array.
 * This also correctly handles image rotation based on EXIF data.
 */
fun downsampleImageFromUri(context: Context, uri: Uri, maxDimension: Int): ByteArray? {
    try {
        // First pass: Decode with inJustDecodeBounds=true to check dimensions without loading image into memory.
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // Calculate the optimal inSampleSize to downsample the image.
        options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
        options.inJustDecodeBounds = false

        // Second pass: Decode the downsampled bitmap.
        val resizedBitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null // Return null if bitmap decoding fails.

        // Third pass: Read EXIF orientation data and rotate the bitmap if necessary.
        val finalBitmap = context.contentResolver.openInputStream(uri)?.use {
            val exif = ExifInterface(it)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            rotateBitmap(resizedBitmap, orientation)
        } ?: resizedBitmap // Use the resized bitmap if EXIF data can't be read.

        // Compress the final, rotated bitmap into a JPEG byte array.
        return bitmapToJpegByteArray(finalBitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

/**
 * Calculates the largest power of 2 sample size that is still smaller than the requested dimensions.
 */
private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/**
 * Compresses a Bitmap to a PNG byte array with no quality loss.
 */
fun bitmapToPngByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY_PNG, stream)
    return stream.toByteArray()
}

/**
 * Compresses a Bitmap to a JPEG byte array with optimized quality.
 */
fun bitmapToJpegByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY_JPEG, stream)
    return stream.toByteArray()
}

/**
 * Converts a byte array back into a Bitmap.
 */
fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
    return try {
        // The rotation is handled before the image is converted to a byte array,
        // so no need to check EXIF data again here.
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Rotates a bitmap based on its EXIF orientation tag.
 */
private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1.0f, 1.0f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1.0f, -1.0f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f)
            matrix.preScale(-1.0f, 1.0f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(-90f)
            matrix.preScale(-1.0f, 1.0f)
        }
        else -> return bitmap // Return original bitmap if no rotation is needed.
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

/**
 * Writes a byte array to a temporary file in the cache and returns its content Uri.
 */
fun byteArrayToUri(context: Context, byteArray: ByteArray, fileName: String): Uri? {
    return try {
        val cachePath = File(context.cacheDir, CACHE_IMAGES_DIR)
        cachePath.mkdirs()
        val file = File(cachePath, fileName)
        file.writeBytes(byteArray)
        FileProvider.getUriForFile(context, "${context.packageName}$FILE_PROVIDER_AUTHORITY_SUFFIX", file)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
