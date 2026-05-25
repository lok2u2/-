package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Reads a Uri, resizes the image to a maximum dimension (e.g., 600px) to keep memory usage extremely low
     * and compiles it to a compact JPEG Base64 string.
     */
    fun uriToBase64(context: Context, uri: Uri, maxDimension: Int = 600): String? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            // First decode with inJustDecodeBounds to find dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size
            var sampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / sampleSize >= maxDimension && halfWidth / sampleSize >= maxDimension) {
                    sampleSize *= 2
                }
            }

            // Decode bitmap with inSampleSize
            inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val scaleOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val originalBitmap = BitmapFactory.decodeStream(inputStream, null, scaleOptions)
            inputStream.close()
            
            if (originalBitmap == null) return null

            val rawWidth = originalBitmap.width
            val rawHeight = originalBitmap.height
            
            // Check EXIF rotation first
            var rotationDegrees = 0
            try {
                context.contentResolver.openInputStream(uri)?.use { exifStream ->
                    val exifInterface = ExifInterface(exifStream)
                    val orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    rotationDegrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } catch (e: Exception) {
                // ignore EXIF error
            }

            // Resize the bitmap precisely so the largest side is exactly maxDimension or smaller
            val longestSide = maxOf(rawWidth, rawHeight)
            val scale = if (longestSide > maxDimension) {
                maxDimension.toFloat() / longestSide
            } else {
                1.0f
            }

            val matrix = Matrix()
            if (scale < 1.0f) {
                matrix.postScale(scale, scale)
            }
            if (rotationDegrees != 0) {
                matrix.postRotate(rotationDegrees.toFloat())
            }

            val finalBitmap = if (scale < 1.0f || rotationDegrees != 0) {
                val resized = Bitmap.createBitmap(
                    originalBitmap, 0, 0, rawWidth, rawHeight, matrix, true
                )
                if (resized != originalBitmap) {
                    originalBitmap.recycle()
                }
                resized
            } else {
                originalBitmap
            }

            // Compress to JPEG standard quality to keep size tiny (~30kb!)
            val baos = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val bytes = baos.toByteArray()
            finalBitmap.recycle()

            return Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                inputStream?.close()
            } catch (e: Exception) {}
        }
    }

    /**
     * Decodes Base64 string back to Bitmap for visualization
     */
    fun base64ToBitmap(base64Str: String?): Bitmap? {
        if (base64Str.isNullOrBlank()) return null
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
