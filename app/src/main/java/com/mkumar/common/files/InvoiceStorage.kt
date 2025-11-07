// Kotlin
package com.mkumar.common.files

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

private const val RELATIVE_DIR = "Documents/MKumar/Invoices"

fun findExistingInvoiceUri(context: Context, fileName: String): Uri? {
    val projection = arrayOf(
        MediaStore.Downloads._ID,
        MediaStore.Downloads.DISPLAY_NAME,
        MediaStore.Downloads.RELATIVE_PATH
    )
    return context.contentResolver.query(
        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Downloads.DISPLAY_NAME}=? AND ${MediaStore.Downloads.RELATIVE_PATH}=?",
        arrayOf(fileName, "$RELATIVE_DIR/"),
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString())
        } else null
    }
}

fun saveInvoicePdf(context: Context, fileName: String, bytes: ByteArray): Uri {
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(MediaStore.Downloads.RELATIVE_PATH, RELATIVE_DIR)
        put(MediaStore.Downloads.IS_PENDING, 1)
    }
    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        ?: error("Failed to create invoice entry")
    context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
    values.clear()
    values.put(MediaStore.Downloads.IS_PENDING, 0)
    context.contentResolver.update(uri, values, null, null)
    return uri
}
