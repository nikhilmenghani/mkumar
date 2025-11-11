package com.mkumar.domain.invoice

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.mkumar.common.constant.CustomerDetailsConstants
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class InvoicePdfBuilderImpl @Inject constructor() : InvoicePdfBuilder {

    override fun build(data: InvoiceData): ByteArray {
        val doc = PdfDocument()
        val out = ByteArrayOutputStream()

        // --- Page metrics (A4 points) ---
        val pageWidth = 595
        val pageHeight = 842
        val marginL = 36f
        val marginR = 36f
        val marginT = 36f
        val marginB = 36f
        val contentW = pageWidth - marginL - marginR

        // --- Typography ---
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            color = Color.BLACK
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11.5f
            color = Color.DKGRAY
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            color = Color.BLACK
        }
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            color = Color.BLACK
        }
        val tableTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11.5f
            typeface = Typeface.MONOSPACE
            color = Color.BLACK
        }
        val boldRightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12.5f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            color = Color.BLACK
            textAlign = Paint.Align.RIGHT
        }
        val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            color = Color.BLACK
            textAlign = Paint.Align.RIGHT
        }
        val faintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 1f
            color = Color.LTGRAY
        }

        // --- Currency formatter (INR) ---
        val moneyFmt = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            currency = Currency.getInstance("INR")
            maximumFractionDigits = if (hasCents(data)) 2 else 0
            minimumFractionDigits = maximumFractionDigits
        }

        // --- Table columns (x positions) ---
        val colItemX = marginL + 0f
        val colQtyX = marginL + contentW * 0.44f
        val colUnitX = marginL + contentW * 0.58f
        val colDiscountX = marginL + contentW * 0.76f
        val colTotalX = marginL + contentW * 0.92f

        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
        var c = page.canvas
        var y = marginT

        fun newPageIfNeeded(nextBlockHeight: Float) {
            if (y + nextBlockHeight > pageHeight - marginB) {
                doc.finishPage(page)
                pageNum += 1
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
                c = page.canvas
                y = marginT
                // draw header again on new page
                y = drawHeader(c, data, titlePaint, labelPaint, textPaint, faintLine, marginL, marginR, y, contentW)
                // draw table header again
                y = drawTableHeader(c, tableHeaderPaint, faintLine, colItemX, colQtyX, colUnitX, colDiscountX, colTotalX, y, marginL, marginR)
            }
        }

        // Header
        y = drawHeader(c, data, titlePaint, labelPaint, textPaint, faintLine, marginL, marginR, y, contentW)

        // Table header
        y = drawTableHeader(c, tableHeaderPaint, faintLine, colItemX, colQtyX, colUnitX, colDiscountX, colTotalX, y, marginL, marginR)

        // Table rows
        val rowHeight = 16f
        data.items.forEach { row ->
            newPageIfNeeded(rowHeight + 8f)

            // Item name (trim if too long)
            val maxItemWidth = colQtyX - colItemX - 8f
            val itemName = ellipsize(row.name, tableTextPaint, maxItemWidth)

            c.drawText(row.description, colItemX, y, tableTextPaint)
            c.drawText(row.qty.toString(), colQtyX, y, rightPaint)
            c.drawText(moneyFmt.format(row.unitPrice), colUnitX, y, rightPaint)
            val discountText = if (row.discount > 0.0) "${row.discount}%" else "-"
            c.drawText(discountText, colDiscountX, y, rightPaint)
            c.drawText(moneyFmt.format(row.total), colTotalX, y, rightPaint)
            y += rowHeight
        }

        // Divider before totals
        newPageIfNeeded(28f)
        c.drawLine(marginL, y, pageWidth - marginR, y, faintLine)
        y += 12f

        // Totals block (right-aligned)
        fun totalRow(label: String, value: Double, bold: Boolean = false) {
            val paint = if (bold) boldRightPaint else rightPaint
            val labelPaintR = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }
            c.drawText(label, colUnitX, y, labelPaintR)
            c.drawText(moneyFmt.format(value), colTotalX, y, paint)
            y += 16f
        }

        totalRow("Subtotal", data.subtotal)
        if (data.adjustedTotal != 0.0) totalRow("Adjusted Total", -kotlin.math.abs(data.adjustedTotal))
        if (data.advanceTotal != 0.0) totalRow("Advance Total", data.advanceTotal)
        totalRow("Remaining Balance", data.remainingBalance, bold = true)

        // Footer line
        y += 8f
        c.drawLine(marginL, y, pageWidth - marginR, y, faintLine)

        doc.finishPage(page)
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    // --- Helpers ---

    private fun drawHeader(
        c: Canvas,
        data: InvoiceData,
        titlePaint: Paint,
        labelPaint: Paint,
        textPaint: Paint,
        linePaint: Paint,
        marginL: Float,
        marginR: Float,
        startY: Float,
        contentW: Float
    ): Float {
        var y = startY

        // Shop title & address
        c.drawText(data.shopName, marginL, y, titlePaint)
        y += 20f
        if (data.shopAddress.isNotBlank()) {
            wrapText(c, data.shopAddress, textPaint, marginL, (marginL + contentW), y).also { y = it }
            y += 2f
        }

        // Divider
        y += 6f
        c.drawLine(marginL, y, (marginL + contentW), y, linePaint)
        y += 14f
        // Meta
        c.drawText("Invoice: ${CustomerDetailsConstants.getInvoiceFileName(data.orderId)}", marginL, y, textPaint)
        val right = marginL + contentW
        val rightAlign = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }
        c.drawText("Date: ${data.occurredAtText}", right, y, rightAlign)
        y += 16f

        c.drawText("Customer: ${data.customerName}", marginL, y, textPaint)
        y += 16f
        c.drawText("Phone: ${data.customerPhone}", marginL, y, textPaint)
        y += 12f

        // Divider
        c.drawLine(marginL, y, (marginL + contentW), y, linePaint)
        y += 14f

        return y
    }

    private fun drawTableHeader(
        c: Canvas,
        headerPaint: Paint,
        linePaint: Paint,
        colItemX: Float,
        colQtyX: Float,
        colUnitX: Float,
        colDiscountX: Float,
        colTotalX: Float,
        startY: Float,
        marginL: Float,
        marginR: Float
    ): Float {
        var y = startY
        c.drawText("Item", colItemX, y, headerPaint)
        c.drawText("Qty", colQtyX, y, headerPaint.apply { textAlign = Paint.Align.RIGHT })
        headerPaint.textAlign = Paint.Align.RIGHT
        c.drawText("Unit", colUnitX, y, headerPaint)
        c.drawText("Discount %", colDiscountX, y, headerPaint)
        c.drawText("Total", colTotalX, y, headerPaint)
        y += 12f
        c.drawLine(marginL, y, (595 - marginR), y, linePaint)
        y += 10f
        headerPaint.textAlign = Paint.Align.LEFT
        return y
    }

    private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var low = 0
        var high = text.length
        var best = "…"
        while (low <= high) {
            val mid = (low + high) / 2
            val candidate = text.take(mid) + "…"
            if (paint.measureText(candidate) <= maxWidth) {
                best = candidate
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return best
    }

    private fun wrapText(
        c: Canvas,
        text: String,
        paint: Paint,
        xLeft: Float,
        xRight: Float,
        startY: Float,
        lineSpacing: Float = 14f
    ): Float {
        val words = text.split(' ')
        val maxWidth = xRight - xLeft
        var line = StringBuilder()
        var y = startY

        for (w in words) {
            val trial = if (line.isEmpty()) w else line.toString() + " " + w
            if (paint.measureText(trial) <= maxWidth) {
                line.clear(); line.append(trial)
            } else {
                c.drawText(line.toString(), xLeft, y, paint)
                y += lineSpacing
                line.clear(); line.append(w)
            }
        }
        if (line.isNotEmpty()) {
            c.drawText(line.toString(), xLeft, y, paint)
            y += lineSpacing
        }
        return y
    }

    private fun hasCents(data: InvoiceData): Boolean {
        if (data.subtotal % 1.0 != 0.0) return true
        return data.items.any { (it.unitPrice % 1.0 != 0.0) || (it.total % 1.0 != 0.0) }
    }
}
