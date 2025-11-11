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

        val pageWidth = 595
        val pageHeight = 842
        val insets = Insets(36f, 36f, 36f, 36f)

        val typo = Typography()
        val rules = Rules()
        val money = MoneyFormatter.inr(hasCents(data))

        val pager = Pager(doc, pageWidth, pageHeight, insets)

        // Page 1
        pager.startPage()
        HeaderSection.draw(pager, data, typo, rules)

        // Items header
        ItemsSection.drawHeader(pager, typo, rules)

        // Items rows (auto page-break; re-draws headers on new pages)
        ItemsSection.drawRows(pager, data, money, typo, rules)

        // Totals
        TotalsSection.draw(pager, data, money, typo, rules)

        // Footer rule
        pager.space(8f)
        pager.lineAcross(rules.faintLine)

        pager.finishAndWrite(out)
        doc.close()
        return out.toByteArray()
    }

    // --------------------------
    // Primitives & styling
    // --------------------------

    private data class Insets(val left: Float, val right: Float, val top: Float, val bottom: Float)

    private class Typography {
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            color = Color.BLACK
        }
        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11.5f
            color = Color.DKGRAY
        }
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            color = Color.BLACK
        }
        val tableHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            color = Color.BLACK
            textAlign = Paint.Align.LEFT
        }
        val tableText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11.5f
            typeface = Typeface.MONOSPACE
            color = Color.BLACK
            textAlign = Paint.Align.LEFT
        }
        val right = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            color = Color.BLACK
            textAlign = Paint.Align.RIGHT
        }
        val rightBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12.5f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            color = Color.BLACK
            textAlign = Paint.Align.RIGHT
        }
    }

    private class Rules {
        val faintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 1f
            color = Color.LTGRAY
        }
    }

    // --------------------------
    // Pager (pagination + cursor)
    // --------------------------

    private class Pager(
        private val doc: PdfDocument,
        private val pageWidth: Int,
        private val pageHeight: Int,
        private val insets: Insets
    ) {
        lateinit var canvas: Canvas
        private var pageNum = 0
        private lateinit var page: PdfDocument.Page

        private val contentRight get() = pageWidth - insets.right
        val contentLeft get() = insets.left
        val contentWidth get() = pageWidth - insets.left - insets.right
        private val bottomLimit get() = pageHeight - insets.bottom

        var y: Float = insets.top

        fun startPage() {
            pageNum += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
            canvas = page.canvas
            y = insets.top
        }

        fun ensure(nextBlockHeight: Float, onNewPage: (() -> Unit)? = null) {
            if (y + nextBlockHeight > bottomLimit) {
                finishPage()
                startPage()
                onNewPage?.invoke()
            }
        }

        fun space(dy: Float) { y += dy }

        fun lineAcross(paint: Paint) {
            canvas.drawLine(contentLeft, y, contentRight, y, paint)
        }

        fun finishPage() {
            doc.finishPage(page)
        }

        fun finishAndWrite(out: ByteArrayOutputStream) {
            // finish current page
            finishPage()
            doc.writeTo(out)
        }
    }

    // --------------------------
    // Table DSL
    // --------------------------

    private enum class Align { LEFT, RIGHT }

    private data class ColumnSpec(
        val title: String,
        val widthFraction: Float,
        val align: Align = Align.LEFT,
        val padLeft: Float = 0f,
        val padRight: Float = 0f
    )

    private data class TableSpec(val columns: List<ColumnSpec>) {
        val totalFractions = columns.sumOf { it.widthFraction.toDouble() }.toFloat()
    }

    private class TableDrawer(
        private val pager: Pager,
        private val spec: TableSpec,
        private val headerPaint: Paint,
        private val bodyPaint: Paint,
        private val rules: Rules
    ) {
        private val colBounds: List<Pair<Float, Float>> by lazy {
            val w = pager.contentWidth
            var x = pager.contentLeft
            spec.columns.map { col ->
                val colW = (col.widthFraction / spec.totalFractions) * w
                val left = x + col.padLeft
                val right = x + colW - col.padRight
                x += colW
                left to right
            }
        }

        fun header(rowHeight: Float = 14f, gapBelow: Float = 10f) {
            pager.ensure(rowHeight + 2 + gapBelow)
            spec.columns.forEachIndexed { i, col ->
                val (l, r) = colBounds[i]
                val x = if (col.align == Align.RIGHT) r else l
                val p = Paint(headerPaint).apply {
                    textAlign = if (col.align == Align.RIGHT) Paint.Align.RIGHT else Paint.Align.LEFT
                }
                pager.canvas.drawText(col.title, x, pager.y, p)
            }
            pager.space(rowHeight)
            pager.lineAcross(rules.faintLine)
            pager.space(gapBelow)
        }

        fun row(cells: List<String>, rowHeight: Float = 16f) {
            pager.ensure(rowHeight)
            spec.columns.forEachIndexed { i, col ->
                val (l, r) = colBounds[i]
                val p = Paint(bodyPaint).apply {
                    textAlign = if (col.align == Align.RIGHT) Paint.Align.RIGHT else Paint.Align.LEFT
                }
                val x = if (col.align == Align.RIGHT) r else l
                pager.canvas.drawText(cells.getOrElse(i) { "" }, x, pager.y, p)
            }
            pager.space(rowHeight)
        }

        fun ellipsizedRow(cells: List<String>, rowHeight: Float = 16f) {
            pager.ensure(rowHeight)
            spec.columns.forEachIndexed { i, col ->
                val (l, r) = colBounds[i]
                val p = Paint(bodyPaint).apply {
                    textAlign = if (col.align == Align.RIGHT) Paint.Align.RIGHT else Paint.Align.LEFT
                }
                val x = if (col.align == Align.RIGHT) r else l
                val maxW = (r - l)
                val txt = TextUtil.ellipsize(cells.getOrElse(i) { "" }, p, maxW)
                pager.canvas.drawText(txt, x, pager.y, p)
            }
            pager.space(rowHeight)
        }
    }

    // --------------------------
    // Sections
    // --------------------------

    private object HeaderSection {
        // In HeaderSection
        fun draw(pager: Pager, data: InvoiceData, typo: Typography, rules: Rules) {
            val left = pager.contentLeft
            val right = left + pager.contentWidth
            val mid = left + pager.contentWidth * 0.5f
            val colGap = 16f
            val colWidth = (pager.contentWidth - colGap) / 2

            // Split shop name
            val shopNameLines = listOf(
                "M Kumar",
                "Luxurious Watch & Optical Store"
            )

            // Prepare left column lines
            val leftLines = mutableListOf<String>()
            leftLines.addAll(shopNameLines)
            if (data.customerPhone.isNotBlank()) leftLines.add("Phone: ${data.customerPhone}")
            if (data.customerEmail.isNotBlank()) leftLines.add("Email: ${data.customerEmail}")

            // Calculate max lines for alignment
            val addressLines = wrapTextLines(data.shopAddress, typo.text, colWidth)
            val maxLines = maxOf(leftLines.size, addressLines.size)

            // Draw both columns line by line
            var y = pager.y
            for (i in 0 until maxLines) {
                // Left column
                leftLines.getOrNull(i)?.let { line ->
                    pager.canvas.drawText(line, left, y, typo.title)
                }
                // Right column
                addressLines.getOrNull(i)?.let { line ->
                    pager.canvas.drawText(line, mid + colGap, y, typo.text)
                }
                y += 18f // line height
            }
            pager.y = y

            // Divider
            pager.space(6f)
            pager.lineAcross(rules.faintLine)
            pager.space(14f)

            // Meta row
            pager.canvas.drawText(
                "Invoice: ${CustomerDetailsConstants.getInvoiceFileName(data.orderId)}",
                left, pager.y, typo.text
            )
            val rightAlign = Paint(typo.text).apply { textAlign = Paint.Align.RIGHT }
            pager.canvas.drawText("Date: ${data.occurredAtText}", right, pager.y, rightAlign)
            pager.space(16f)

            // Customer info
            pager.canvas.drawText("Customer: ${data.customerName}", left, pager.y, typo.text)
            pager.space(16f)
            pager.canvas.drawText("Phone: ${data.customerPhone}", left, pager.y, typo.text)
            pager.space(12f)

            // Divider
            pager.lineAcross(rules.faintLine)
            pager.space(14f)
        }

        fun wrapTextLines(
            text: String,
            paint: Paint,
            maxWidth: Float
        ): List<String> {
            if (text.isBlank()) return emptyList()
            val words = text.split(' ')
            val lines = mutableListOf<String>()
            var line = StringBuilder()
            for (w in words) {
                val trial = if (line.isEmpty()) w else line.toString() + " " + w
                if (paint.measureText(trial) <= maxWidth) {
                    line.clear(); line.append(trial)
                } else {
                    if (line.isNotEmpty()) lines.add(line.toString())
                    line = StringBuilder(w)
                }
            }
            if (line.isNotEmpty()) lines.add(line.toString())
            return lines
        }
    }

    private object ItemsSection {
        // Edit columns here to add/remove/resize
        private val spec = TableSpec(
            listOf(
                ColumnSpec(title = "Item",       widthFraction = 0.44f, align = Align.LEFT,  padRight = 8f),
                ColumnSpec(title = "Qty",        widthFraction = 0.14f, align = Align.RIGHT),
                ColumnSpec(title = "Unit",       widthFraction = 0.18f, align = Align.RIGHT),
                ColumnSpec(title = "Discount %", widthFraction = 0.12f, align = Align.RIGHT),
                ColumnSpec(title = "Total",      widthFraction = 0.12f, align = Align.RIGHT),
            )
        )

        fun drawHeader(pager: Pager, typo: Typography, rules: Rules) {
            TableDrawer(pager, spec, typo.tableHeader, typo.tableText, rules).header()
        }

        fun drawRows(pager: Pager, data: InvoiceData, money: NumberFormat, typo: Typography, rules: Rules) {
            val table = TableDrawer(pager, spec, typo.tableHeader, typo.tableText, rules)
            val rowHeight = 16f

            data.items.forEachIndexed { index, item ->
                // If we need a new page, we also redraw the header & table header
                pager.ensure(rowHeight + 8f) {
                    HeaderSection.draw(pager, data, typo, rules)
                    drawHeader(pager, typo, rules)
                }

                val unit = money.format(item.unitPrice)
                val disc = if (item.discount > 0.0) "${item.discount}%" else "-"
                val total = money.format(item.total)

                // Ellipsize long names
                table.ellipsizedRow(listOf(item.name, item.qty.toString(), unit, disc, total), rowHeight)
            }
        }
    }

    private object TotalsSection {
        fun draw(pager: Pager, data: InvoiceData, money: NumberFormat, typo: Typography, rules: Rules) {
            // Divider before totals
            pager.ensure(28f)
            pager.lineAcross(rules.faintLine)
            pager.space(12f)

            // Anchor columns (use same relative feel as items table)
            val rightX = pager.contentLeft + pager.contentWidth
            val labelAnchorX = pager.contentLeft + pager.contentWidth * 0.76f

            fun totalRow(label: String, value: Double, bold: Boolean = false) {
                val labelPaint = Paint(typo.text).apply { textAlign = Paint.Align.RIGHT }
                val valuePaint = if (bold) typo.rightBold else typo.right
                pager.canvas.drawText(label, labelAnchorX, pager.y, labelPaint)
                pager.canvas.drawText(money.format(value), rightX, pager.y, valuePaint)
                pager.space(16f)
            }

            totalRow("Subtotal", data.subtotal)
            if (data.adjustedTotal != 0.0) totalRow("Adjusted Total", -kotlin.math.abs(data.adjustedTotal))
            if (data.advanceTotal != 0.0) totalRow("Advance Total", data.advanceTotal)
            totalRow("Remaining Balance", data.remainingBalance, bold = true)
        }
    }

    // --------------------------
    // Text helpers
    // --------------------------

    private object TextUtil {
        fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
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

        fun wrapText(
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

            fun flush() {
                if (line.isNotEmpty()) {
                    c.drawText(line.toString(), xLeft, y, paint)
                    y += lineSpacing
                    line = StringBuilder()
                }
            }

            for (w in words) {
                val trial = if (line.isEmpty()) w else line.toString() + " " + w
                if (paint.measureText(trial) <= maxWidth) {
                    line.clear(); line.append(trial)
                } else {
                    flush()
                    // Hard-break if a single word exceeds width
                    if (paint.measureText(w) > maxWidth) {
                        var idx = 0
                        while (idx < w.length) {
                            var lo = idx
                            var hi = w.length
                            while (lo < hi) {
                                val mid = (lo + hi + 1) / 2
                                val part = w.substring(idx, mid)
                                if (paint.measureText(part) <= maxWidth) lo = mid else hi = mid - 1
                            }
                            val part = w.substring(idx, lo)
                            c.drawText(part, xLeft, y, paint)
                            y += lineSpacing
                            idx = lo
                        }
                    } else {
                        line.append(w)
                    }
                }
            }
            if (line.isNotEmpty()) {
                c.drawText(line.toString(), xLeft, y, paint)
                y += lineSpacing
            }
            return y
        }
    }

    // --------------------------
    // Money
    // --------------------------

    private object MoneyFormatter {
        fun inr(twoDecimals: Boolean): NumberFormat {
            return NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
                currency = Currency.getInstance("INR")
                maximumFractionDigits = if (twoDecimals) 2 else 0
                minimumFractionDigits = maximumFractionDigits
            }
        }
    }

    private fun hasCents(data: InvoiceData): Boolean {
        if (data.subtotal % 1.0 != 0.0) return true
        return data.items.any { (it.unitPrice % 1.0 != 0.0) || (it.total % 1.0 != 0.0) }
    }
}
