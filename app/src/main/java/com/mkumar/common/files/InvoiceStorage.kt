// com.mkumar.common.files.InvoiceStorage.kt
package com.mkumar.common.files

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

private const val RELATIVE_DIR = "Documents/MKumar/Invoices"

fun findExistingInvoiceUri(context: Context, fileName: String): Uri? {
    return if (Build.VERSION.SDK_INT >= 29) {
        val projection = arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME, MediaStore.Downloads.RELATIVE_PATH)
        context.contentResolver.query(
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
    } else {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "MKumar/Invoices/$fileName"
        )
        if (file.exists()) FileProvider.getUriForFile(context, "${context.packageName}.provider", file) else null
    }
}

fun saveInvoicePdf(context: Context, fileName: String, bytes: ByteArray): Uri {
    return if (Build.VERSION.SDK_INT >= 29) {
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
        uri
    } else {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "MKumar/Invoices"
        ).apply { mkdirs() }
        val file = File(dir, fileName)
        file.outputStream().use { it.write(bytes) }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
}
