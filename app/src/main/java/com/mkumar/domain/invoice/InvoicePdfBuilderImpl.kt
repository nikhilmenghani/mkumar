// com.mkumar.domain.invoice.InvoicePdfBuilderImpl.kt
package com.mkumar.domain.invoice

import android.graphics.*
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.max
import java.text.NumberFormat
import java.util.Locale

class InvoicePdfBuilderImpl @Inject constructor() : InvoicePdfBuilder {
    override fun build(data: InvoiceData): ByteArray {
        val doc = PdfDocument()
        val pageWidth = 595; val pageHeight = 842
        var y = 40

        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        val canvas = page.canvas

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD; textSize = 18f }
        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD; textSize = 11f }
        val text  = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f }
        val rule  = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        val nf = NumberFormat.getIntegerInstance(Locale("en","IN"))
        fun money(v: Int) = "${data.currencySymbol}${nf.format(v)}"
        fun draw(p: Paint, s: String, x: Int, yy: Int) = canvas.drawText(s, x.toFloat(), yy.toFloat(), p)

        // Header
        draw(title, data.shopName, 40, y); y += 18
        data.shopAddress?.let { draw(text, it, 40, y); y += 14 }
        data.shopPhone?.let { draw(text, "Phone: $it", 40, y); y += 20 }
        canvas.drawLine(40f, y.toFloat(), (pageWidth-40).toFloat(), y.toFloat(), rule); y += 18

        // Bill To + meta
        draw(label, "Bill To:", 40, y); y += 14
        draw(text, data.customerName, 40, y); y += 14
        data.customerPhone?.let { draw(text, it, 40, y); y += 14 }

        val rx = pageWidth - 220
        draw(label, "Order ID:", rx, 90); draw(text, data.orderId, rx, 104)
        draw(label, "Date:", rx, 122); draw(text, data.occurredAt.toLocalDate().toString(), rx, 136)

        y = max(y, 160)
        canvas.drawLine(40f, y.toFloat(), (pageWidth-40).toFloat(), y.toFloat(), rule); y += 20

        // Table
        val col = intArrayOf(40, 280, 350, 430, 520) // Item | Qty | Unit | Disc% | Total
        draw(label, "Item", col[0], y)
        draw(label, "Qty", col[1], y)
        draw(label, "Unit", col[2], y)
        draw(label, "Disc", col[3], y)
        draw(label, "Total", col[4]-60, y)
        y += 10; canvas.drawLine(40f, y.toFloat(), (pageWidth-40).toFloat(), y.toFloat(), rule); y += 16

        data.items.forEach { it ->
            draw(text, it.name.take(42), col[0], y)
            draw(text, it.quantity.toString(), col[1], y)
            draw(text, money(it.unitPrice), col[2], y)
            draw(text, "${it.discountPercentage}%", col[3], y)
            draw(text, money(it.lineTotal), col[4]-60, y)
            y += 16
        }

        // Totals
        val top = y + 10; val tx = pageWidth - 240
        fun row(lbl: String, valStr: String, yy: Int, bold: Boolean=false) {
            val p = if (bold) label else text
            draw(p, lbl, tx, yy); draw(p, valStr, tx+140, yy)
        }
        row("Subtotal", money(data.subtotalBeforeAdjust), top)
        row("Adjustment", "- ${money(data.adjustedAmount)}", top+16)
        row("Advance", "- ${money(data.advanceTotal)}", top+32)
        canvas.drawLine(tx.toFloat(), (top+42).toFloat(), (tx+200).toFloat(), (top+42).toFloat(), rule)
        row("TOTAL", money(data.totalAmount), top+60, bold = true)
        row("Remaining", money(data.remainingBalance), top+76, bold = true)

        // Footer
        canvas.drawLine(40f, (pageHeight-60).toFloat(), (pageWidth-40).toFloat(), (pageHeight-60).toFloat(), rule)
        draw(small, "Thank you for your business!", 40, pageHeight-40)

        doc.finishPage(page)
        return ByteArrayOutputStream().use { bos -> doc.writeTo(bos); doc.close(); bos.toByteArray() }
    }
}
