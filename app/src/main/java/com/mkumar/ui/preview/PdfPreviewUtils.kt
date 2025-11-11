// file: com/mkumar/ui/preview/PdfPreviewUtils.kt
package com.mkumar.ui.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import java.io.File

fun renderPdfToBitmaps(context: Context, pdfBytes: ByteArray): List<Bitmap> {
    // Write bytes to a temp file so PdfRenderer can read it
    val tmp = File.createTempFile("invoice_preview_", ".pdf", context.cacheDir)
    tmp.outputStream().use { it.write(pdfBytes) }

    val bitmaps = mutableListOf<Bitmap>()
    val pfd = ParcelFileDescriptor.open(tmp, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(pfd)
    try {
        for (i in 0 until renderer.pageCount) {
            renderer.openPage(i).use { page ->
                // create a bitmap with page size (you can scale if needed)
                val bmp = createBitmap(page.width, page.height)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps += bmp
            }
        }
    } finally {
        renderer.close()
        pfd.close()
        // keep the tmp file; OS will clear cache eventually. Or delete if you prefer.
        // tmp.delete()
    }
    return bitmaps
}
