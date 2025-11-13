package com.mkumar.domain.invoice

import android.graphics.Bitmap
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

        val tableBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
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
            finishPage()
            doc.writeTo(out)
        }
    }

    // --------------------------
    // Table DSL
    // --------------------------

    private enum class Align { LEFT, RIGHT, CENTER }

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
        val spec: TableSpec,
        private val headerPaint: Paint,
        private val bodyPaint: Paint,
        private val rules: Rules
    ) {
        private val cellPaddingX = 4f

        val colBounds: List<Pair<Float, Float>> by lazy {
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

        private fun tableLeft(): Float =
            colBounds.first().first - spec.columns.first().padLeft

        private fun tableRight(): Float =
            colBounds.last().second + spec.columns.last().padRight

        fun drawRowBorders(top: Float, bottom: Float) {
            val tableLeft = tableLeft()
            val tableRight = tableRight()

            // Outer rect
            pager.canvas.drawRect(tableLeft, top, tableRight, bottom, rules.tableBorder)

            // Vertical grid lines
            var x = tableLeft
            val totalWidth = pager.contentWidth
            spec.columns.forEachIndexed { index, col ->
                val colW = (col.widthFraction / spec.totalFractions) * totalWidth
                x += colW
                if (index < spec.columns.lastIndex) {
                    pager.canvas.drawLine(x, top, x, bottom, rules.tableBorder)
                }
            }
        }

        fun header(rowHeight: Float = 18f, gapBelow: Float = 0f) {
            pager.ensure(rowHeight + gapBelow)

            val rowTop = pager.y
            val rowBottom = rowTop + rowHeight
            val textY = rowTop + rowHeight / 2 - (headerPaint.descent() + headerPaint.ascent()) / 2

            spec.columns.forEachIndexed { i, col ->
                val (l, r) = colBounds[i]
                val p = Paint(headerPaint)
                val x = when (col.align) {
                    Align.LEFT -> {
                        p.textAlign = Paint.Align.LEFT
                        l + cellPaddingX
                    }
                    Align.RIGHT -> {
                        p.textAlign = Paint.Align.RIGHT
                        r - cellPaddingX
                    }
                    Align.CENTER -> {
                        p.textAlign = Paint.Align.CENTER
                        (l + r) / 2f
                    }
                }
                pager.canvas.drawText(col.title, x, textY, p)
            }

            drawRowBorders(rowTop, rowBottom)
            pager.space(rowHeight)
            pager.space(gapBelow)
        }

        fun ellipsizedRow(cells: List<String>, rowHeight: Float = 18f) {
            pager.ensure(rowHeight)
            val rowTop = pager.y
            val rowBottom = rowTop + rowHeight
            val textY = rowTop + rowHeight / 2 - (bodyPaint.descent() + bodyPaint.ascent()) / 2

            spec.columns.forEachIndexed { i, col ->
                val (l, r) = colBounds[i]
                val p = Paint(bodyPaint)
                val x = when (col.align) {
                    Align.LEFT -> {
                        p.textAlign = Paint.Align.LEFT
                        l + cellPaddingX
                    }
                    Align.RIGHT -> {
                        p.textAlign = Paint.Align.RIGHT
                        r - cellPaddingX
                    }
                    Align.CENTER -> {
                        p.textAlign = Paint.Align.CENTER
                        (l + r) / 2f
                    }
                }
                val maxW = (r - l) - 2 * cellPaddingX
                val txt = TextUtil.ellipsize(cells.getOrElse(i) { "" }, p, maxW)
                pager.canvas.drawText(txt, x, textY, p)
            }

            drawRowBorders(rowTop, rowBottom)
            pager.space(rowHeight)
        }

        /**
         * Special row where first column has title + subtext (two lines),
         * and remaining columns are single-line centered vertically.
         */
        fun itemRowWithSubtext(
            title: String,
            subtext: String?,
            otherCells: List<String>,
            rowHeight: Float = 28f
        ) {
            pager.ensure(rowHeight)
            val rowTop = pager.y
            val rowBottom = rowTop + rowHeight

            // Column 0: title + subtext
            val (l0, r0) = colBounds[0]
            val maxW0 = (r0 - l0) - 2 * cellPaddingX

            val titlePaint = Paint(bodyPaint).apply {
                textAlign = Paint.Align.LEFT
            }
            val subPaint = Paint(bodyPaint).apply {
                textAlign = Paint.Align.LEFT
                textSize = bodyPaint.textSize - 1f
                color = Color.DKGRAY
            }

            val titleHeight = titlePaint.descent() - titlePaint.ascent()
            val subHeight = if (subtext.isNullOrBlank()) 0f else (subPaint.descent() - subPaint.ascent())
            val lineSpacing = if (subtext.isNullOrBlank()) 0f else 2f

            val textBlockHeight = titleHeight + subHeight + lineSpacing
            val rowCenter = rowTop + rowHeight / 2f
            val firstBaseline = rowCenter - textBlockHeight / 2f - titlePaint.ascent()

            val x0 = l0 + cellPaddingX
            val titleText = TextUtil.ellipsize(title, titlePaint, maxW0)
            pager.canvas.drawText(titleText, x0, firstBaseline, titlePaint)

            if (!subtext.isNullOrBlank()) {
                val secondBaseline = firstBaseline + titleHeight + lineSpacing
                val subText = TextUtil.ellipsize(subtext, subPaint, maxW0)
                pager.canvas.drawText(subText, x0, secondBaseline, subPaint)
            }

            // Remaining columns: single line, vertically centered
            val baseIndex = 1
            val textCenterY = rowTop + rowHeight / 2 - (bodyPaint.descent() + bodyPaint.ascent()) / 2

            spec.columns.drop(baseIndex).forEachIndexed { localIndex, col ->
                val i = baseIndex + localIndex
                val (l, r) = colBounds[i]
                val p = Paint(bodyPaint)
                val x = when (col.align) {
                    Align.LEFT -> {
                        p.textAlign = Paint.Align.LEFT
                        l + cellPaddingX
                    }
                    Align.RIGHT -> {
                        p.textAlign = Paint.Align.RIGHT
                        r - cellPaddingX
                    }
                    Align.CENTER -> {
                        p.textAlign = Paint.Align.CENTER
                        (l + r) / 2f
                    }
                }
                val text = otherCells.getOrElse(localIndex) { "" }
                val maxW = (r - l) - 2 * cellPaddingX
                val txt = TextUtil.ellipsize(text, p, maxW)
                pager.canvas.drawText(txt, x, textCenterY, p)
            }

            drawRowBorders(rowTop, rowBottom)
            pager.space(rowHeight)
        }
    }

    // --------------------------
    // Sections
    // --------------------------

    private object HeaderSection {
        fun draw(pager: Pager, data: InvoiceData, typo: Typography, rules: Rules) {
            val c = pager.canvas
            val centerX = pager.contentLeft + pager.contentWidth / 2f
            var y = pager.y

            // --- Logo + Title row ---
            val logoSize = 40f
            val logoLeft = pager.contentLeft
            val logoTop = y
            val logoRight = logoLeft + logoSize
            val logoBottom = logoTop + logoSize

            val logoRectLeft = logoLeft
            val logoRectTop = logoTop
            val logoRectRight = logoRight
            val logoRectBottom = logoBottom

            val logoBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.DKGRAY
            }
            c.drawRoundRect(logoRectLeft, logoRectTop, logoRectRight, logoRectBottom, 8f, 8f, logoBorderPaint)

            // Use app logo if available, else placeholder "M"
            val logoBitmap: Bitmap? = null
            if (logoBitmap != null) {
                val srcW = logoBitmap.width.toFloat()
                val srcH = logoBitmap.height.toFloat()
                val scale = minOf(
                    (logoRectRight - logoRectLeft) / srcW,
                    (logoRectBottom - logoRectTop) / srcH
                )
                val destW = srcW * scale
                val destH = srcH * scale
                val dx = (logoRectLeft + logoRectRight - destW) / 2f
                val dy = (logoRectTop + logoRectBottom - destH) / 2f
                c.drawBitmap(
                    Bitmap.createScaledBitmap(
                        logoBitmap,
                        destW.toInt().coerceAtLeast(1),
                        destH.toInt().coerceAtLeast(1),
                        true
                    ),
                    dx,
                    dy,
                    null
                )
            } else {
                val logoTextPaint = Paint(typo.text).apply {
                    textAlign = Paint.Align.CENTER
                    textSize = 16f
                    typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                }
                val logoTextY = logoTop + logoSize / 2 - (logoTextPaint.descent() + logoTextPaint.ascent()) / 2
                c.drawText("M", (logoLeft + logoRight) / 2f, logoTextY, logoTextPaint)
            }

            // "M Kumar" centered
            val titlePaint = Paint(typo.title).apply {
                textAlign = Paint.Align.CENTER
            }
            val nameY = logoTop + logoSize / 2 - 4f
            c.drawText("M Kumar", centerX, nameY, titlePaint)

            // "Luxurious Watch & Optical Store" smaller, centered
            val subtitlePaint = Paint(typo.label).apply {
                textAlign = Paint.Align.CENTER
                textSize = 12.5f
            }
            val taglineY = nameY + 16f
            c.drawText("Luxurious Watch & Optical Store", centerX, taglineY, subtitlePaint)

            y = logoBottom + 10f

            // --- Address label + lines ---
            val addressLabelPaint = Paint(typo.label).apply {
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            }
            c.drawText("Address:", centerX, y, addressLabelPaint)
            y += 14f

            val addressLines = listOf(
                "7, Shlok Height, Opp. Dev Paradise & Dharti Silver,",
                "Nr. Mansarovar Road, Chandkheda, Ahmedabad, Gujarat - 382424"
            )
            val addressPaint = Paint(typo.label).apply {
                textAlign = Paint.Align.CENTER
            }
            addressLines.forEach { line ->
                c.drawText(line, centerX, y, addressPaint)
                y += 14f
            }

            // --- Contact info (owner phone/email) ---
            val contactParts = mutableListOf<String>()
            if (data.ownerPhone.isNotBlank()) contactParts.add("Phone: ${data.ownerPhone}")
            if (data.ownerEmail.isNotBlank()) contactParts.add("Email: ${data.ownerEmail}")

            if (contactParts.isNotEmpty()) {
                y += 4f
                val contactLabelPaint = Paint(typo.label).apply {
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                }
                c.drawText("Contact:", centerX, y, contactLabelPaint)
                y += 14f

                val contactPaint = Paint(typo.label).apply {
                    textAlign = Paint.Align.CENTER
                }
                val contactLine = contactParts.joinToString("    ")
                c.drawText(contactLine, centerX, y, contactPaint)
                y += 14f
            }

            pager.y = y
            pager.space(6f)
            pager.lineAcross(rules.faintLine)
            pager.space(10f)

            // --- Big centered "Invoice" title ---
            val invoiceTitlePaint = Paint(typo.title).apply {
                textAlign = Paint.Align.CENTER
                textSize = 20f
            }
            val invoiceTitleY = pager.y + 18f
            c.drawText("Invoice", centerX, invoiceTitleY, invoiceTitlePaint)
            pager.y = invoiceTitleY + 18f
            pager.space(6f)

            // --- Customer & Invoice info section (two columns) ---
            val boldLabel = Paint(typo.label).apply { typeface = Typeface.DEFAULT_BOLD }
            val normalText = typo.text

            val infoRowHeight = 18f
            val leftX = pager.contentLeft
            val rightX = pager.contentLeft + pager.contentWidth

            var infoY = pager.y

            val rightLabel = Paint(boldLabel).apply { textAlign = Paint.Align.RIGHT }
            val rightValue = Paint(normalText).apply { textAlign = Paint.Align.RIGHT }

            // Row 1: Customer name (left) / Invoice # (right)
            c.drawText("Customer: ", leftX, infoY, boldLabel)
            c.drawText(
                data.customerName,
                leftX + boldLabel.measureText("Customer: "),
                infoY,
                normalText
            )

            val invoiceFileName = CustomerDetailsConstants.getInvoiceFileName(
                data.orderId,
                data.invoiceNumber
            )
            val invoiceLabelText = "Invoice #: "
            val invoiceValueWidth = rightValue.measureText(invoiceFileName)
            val invoiceLabelX = rightX - invoiceValueWidth - 4f
            c.drawText(invoiceLabelText, invoiceLabelX, infoY, rightLabel)
            c.drawText(invoiceFileName, rightX, infoY, rightValue)

            infoY += infoRowHeight

            // Row 2: Customer phone (left) / Invoice Generated Date (right)
            val phoneLabel = "Phone: "
            c.drawText(phoneLabel, leftX, infoY, boldLabel)
            c.drawText(
                data.customerPhone,
                leftX + boldLabel.measureText(phoneLabel),
                infoY,
                normalText
            )

            val dateLabelText = "Invoice Generated: "
            val dateValue = data.occurredAtText
            val dateValueWidth = rightValue.measureText(dateValue)
            val dateLabelX = rightX - dateValueWidth - 4f
            c.drawText(dateLabelText, dateLabelX, infoY, rightLabel)
            c.drawText(dateValue, rightX, infoY, rightValue)

            infoY += infoRowHeight

            pager.y = infoY
            pager.space(10f)
            // also no divider here to avoid double-line before table
            pager.space(8f)
        }
    }

    private object ItemsSection {
        // Updated alignment: Item = LEFT, others = CENTER
        private val spec = TableSpec(
            listOf(
                ColumnSpec(title = "Item",       widthFraction = 0.40f, align = Align.LEFT,   padRight = 8f),
                ColumnSpec(title = "Type",       widthFraction = 0.18f, align = Align.CENTER),
                ColumnSpec(title = "Rate",       widthFraction = 0.16f, align = Align.CENTER),
                ColumnSpec(title = "Disc %",     widthFraction = 0.10f, align = Align.CENTER),
                ColumnSpec(title = "Total",      widthFraction = 0.16f, align = Align.CENTER),
            )
        )

        fun drawHeader(pager: Pager, typo: Typography, rules: Rules) {
            TableDrawer(pager, spec, typo.tableHeader, typo.tableText, rules).header()
        }

        fun drawRows(pager: Pager, data: InvoiceData, money: NumberFormat, typo: Typography, rules: Rules) {
            val table = TableDrawer(pager, spec, typo.tableHeader, typo.tableText, rules)

            data.items.forEach { item ->
                val owner = item.owner.takeIf { it.isNotBlank() }
                val hasOwner = (owner != null) && (owner != item.name)
                val rowHeight = 18f

                pager.ensure(rowHeight + 8f) {
                    HeaderSection.draw(pager, data, typo, rules)
                    drawHeader(pager, typo, rules)
                }

                val unit = money.format(item.unitPrice)
                val disc = if (item.discount > 0.0) "${item.discount}%" else "-"
                val total = money.format(item.total)

                // Custom drawing for the first column
                val (l, r) = table.colBounds[0]
                val x0 = l + 4f // cellPaddingX
                val textY = pager.y + rowHeight / 2 - (typo.tableText.descent() + typo.tableText.ascent()) / 2

                // Draw item description
                val titlePaint = Paint(typo.tableText).apply { textAlign = Paint.Align.LEFT }
                pager.canvas.drawText(item.description, x0, textY, titlePaint)

                // Draw owner in smaller font, right after description
                if (hasOwner) {
                    val ownerPaint = Paint(typo.tableText).apply {
                        textAlign = Paint.Align.LEFT
                        textSize = typo.tableText.textSize - 2f
                        color = Color.DKGRAY
                    }
                    val descWidth = titlePaint.measureText(item.description)
                    pager.canvas.drawText(" (${owner})", x0 + descWidth, textY, ownerPaint)
                }

                // Draw other columns as usual
                val otherCells = listOf(item.productType, unit, disc, total)
                table.spec.columns.drop(1).forEachIndexed { idx, col ->
                    val (cl, cr) = table.colBounds[idx + 1]
                    val p = Paint(typo.tableText)
                    val x = when (col.align) {
                        Align.LEFT -> cl + 4f
                        Align.RIGHT -> cr - 4f
                        Align.CENTER -> (cl + cr) / 2f
                    }
                    p.textAlign = when (col.align) {
                        Align.LEFT -> Paint.Align.LEFT
                        Align.RIGHT -> Paint.Align.RIGHT
                        Align.CENTER -> Paint.Align.CENTER
                    }
                    val text = otherCells.getOrElse(idx) { "" }
                    pager.canvas.drawText(text, x, textY, p)
                }

                // Draw row borders and advance
                table.drawRowBorders(pager.y, pager.y + rowHeight)
                pager.space(rowHeight)
            }
        }
    }

    private object TotalsSection {
        fun draw(pager: Pager, data: InvoiceData, money: NumberFormat, typo: Typography, rules: Rules) {
            // Some space before totals
            pager.ensure(28f)
            pager.space(12f)

            // Compute bounds of the "Total" column to align numbers under it
            val colFractions = listOf(0.40f, 0.12f, 0.16f, 0.16f, 0.16f)
            val totalFractions = colFractions.sum()
            var x = pager.contentLeft
            var totalColLeft = x
            var totalColRight = x + pager.contentWidth

            colFractions.forEachIndexed { index, frac ->
                val w = (frac / totalFractions) * pager.contentWidth
                if (index == colFractions.lastIndex) {
                    totalColLeft = x
                    totalColRight = x + w
                }
                x += w
            }

            val valueX = totalColRight - 4f
            val labelX = totalColLeft - 8f

            fun totalRow(label: String, value: Double, bold: Boolean = false) {
                val labelPaint = Paint(typo.text).apply {
                    textAlign = Paint.Align.RIGHT
                }
                val valueCenterX = (totalColLeft + totalColRight) / 2f
                val valuePaint = if (bold) typo.rightBold else typo.right
                valuePaint.textAlign = Paint.Align.CENTER
                pager.canvas.drawText(label, labelX, pager.y, labelPaint)
                pager.canvas.drawText(money.format(value), valueCenterX, pager.y, valuePaint)
                pager.space(16f)
            }

            totalRow("Subtotal", data.subtotal)
            if (data.adjustedTotal != 0.0) totalRow("Adjusted Total", -kotlin.math.abs(data.adjustedTotal))
            if (data.advanceTotal != 0.0) totalRow("Advance Total", data.advanceTotal)
            totalRow("Total Due", data.remainingBalance, bold = true)
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
