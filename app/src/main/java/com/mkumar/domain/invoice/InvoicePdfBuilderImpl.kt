package com.mkumar.domain.invoice

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.mkumar.common.constant.CustomerDetailsConstants
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

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

        // Terms & Conditions (boxed)
        TermsSection.drawBoxed(
            pager,
            termsList,      // configurable list
            typo,
            rules
        )

        pager.finishAndWrite(out)
        doc.close()
        return out.toByteArray()
    }

    // Configurable, grammatically-correct terms
    private val termsList = listOf(
        "Advance payment is mandatory.",
        "Once an order is placed, it cannot be cancelled.",
        "No guarantee on frames or frame color.",
        "No scratch guarantee on lenses."
    )


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
            textSize = 11f  // slightly bigger than body
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)  // sans-serif bold
            color = Color.BLACK
            textAlign = Paint.Align.LEFT
        }
        val tableText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f  // match descriptionSmall
            typeface = Typeface.SANS_SERIF  // sans-serif regular
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
        val descriptionSmall = Paint(text).apply {
            textSize = 10f
            typeface = Typeface.SANS_SERIF
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

        fun ellipsizedRow(
            cells: List<String>,
            rowHeight: Float = 18f,
            descriptionPaint: Paint? = null
        ) {
            pager.ensure(rowHeight)
            val rowTop = pager.y
            val rowBottom = rowTop + rowHeight
            val textY = rowTop + rowHeight / 2 - (bodyPaint.descent() + bodyPaint.ascent()) / 2

            spec.columns.forEachIndexed { i, col ->
                val (l, r) = colBounds[i]
                // Use custom paint for first (description) column if provided
                val basePaint = if (i == 0 && descriptionPaint != null) descriptionPaint else bodyPaint
                val p = Paint(basePaint)
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

        private val advertisedProducts = listOf(
            "Spectacles",
            "Contact Lenses",
            "Sunglasses",
            "Watches",
            "Wall Clocks"
        )

        @SuppressLint("UseKtx")
        fun draw(pager: Pager, data: InvoiceData, typo: Typography, rules: Rules) {

            val c = pager.canvas
            val centerX = pager.contentLeft + pager.contentWidth / 2f
            var y = pager.y

            // ---------- CONFIG ----------
            val logoSize = 82f
            val titleSize = 38f
            val subtitleSize = 16.5f
            val logoSpacing = 32f            // Option B (tweak to 24f or 40f anytime)
            val subtitleGap = 8f             // reduced spacing

            // ---------- PAINTS ----------
            val titlePaint = Paint(typo.title).apply {
                textAlign = Paint.Align.CENTER
                textSize = titleSize
            }
            val subtitlePaint = Paint(typo.label).apply {
                textAlign = Paint.Align.CENTER
                textSize = subtitleSize
            }

            // ------------------------------------------------------------
            // 1) MEASURE TITLE + SUBTITLE BLOCK
            // ------------------------------------------------------------
            val titleHeight = titlePaint.descent() - titlePaint.ascent()
            val subtitleHeight = subtitlePaint.descent() - subtitlePaint.ascent()

            // total block height = title + gap + subtitle
            val titleBlockHeight = titleHeight + subtitleGap + subtitleHeight

            // vertical center of this block
            val titleBlockCenterY = y + titleBlockHeight / 2f

            // TOP of title block baseline calculation
            val titleBaselineY =
                titleBlockCenterY - (titleHeight + subtitleHeight + subtitleGap) / 2f -
                        titlePaint.ascent()

            val subtitleBaselineY =
                titleBaselineY + titlePaint.descent() - subtitlePaint.ascent()+ 2f

            // ------------------------------------------------------------
            // 2) TITLE BLOCK IS CENTERED ON PAGE
            // ------------------------------------------------------------
            val titleBlockCenterX = centerX

            // ------------------------------------------------------------
            // 3) PLACE LOGO LEFT OF CENTERED TITLE BLOCK
            // ------------------------------------------------------------
            val titleBlockHalfWidth = max(
                titlePaint.measureText("M Kumar"),
                subtitlePaint.measureText("Luxurious Watch & Optical Store")
            ) / 2f

            val titleBlockLeft = titleBlockCenterX - titleBlockHalfWidth

            // logo center is logoSize/2 + spacing left of titleBlockLeft
            val logoCenterX = titleBlockLeft - logoSpacing - (logoSize / 2f)

            // logo vertically centered with title block
            val logoCenterY = titleBlockCenterY
            val logoTop = logoCenterY - (logoSize / 2f)

            // ------------------------------------------------------------
            // 4) DRAW LOGO (NO BLUR — MATRIX SCALING)
            // ------------------------------------------------------------
            val bitmap = data.logoBitmap

            if (bitmap != null) {

                val srcW = bitmap.width.toFloat()
                val srcH = bitmap.height.toFloat()

                val scale = minOf(logoSize / srcW, logoSize / srcH)

                val matrix = Matrix()
                matrix.setScale(scale, scale)
                matrix.postTranslate(
                    logoCenterX - (srcW * scale / 2f),
                    logoTop + (logoSize - srcH * scale) / 2f
                )

                c.save()
                c.drawBitmap(bitmap, matrix, null)
                c.restore()

            } else {
                // fallback "M"
                val fallbackPaint = Paint(typo.text).apply {
                    textAlign = Paint.Align.CENTER
                    textSize = 26f
                    typeface = Typeface.DEFAULT_BOLD
                }
                val base =
                    logoCenterY - (fallbackPaint.descent() + fallbackPaint.ascent()) / 2f
                c.drawText("M", logoCenterX, base, fallbackPaint)
            }

            // ------------------------------------------------------------
            // 5) DRAW TITLE + SUBTITLE (CENTERED)
            // ------------------------------------------------------------
            c.drawText("M Kumar", titleBlockCenterX, titleBaselineY, titlePaint)
            c.drawText(
                "Luxurious Watch & Optical Store",
                titleBlockCenterX,
                subtitleBaselineY,
                subtitlePaint
            )

            // move cursor below subtitle
            pager.y = subtitleBaselineY + subtitleHeight + 8f

// Draw product chips under subtitle
            ProductsChipsSection.drawChips(
                pager,
                advertisedProducts,   // bind from outer list
                typo
            )

            pager.lineAcross(rules.faintLine)
            pager.space(20f)

            // ------------------------------------------------------------
            // 6) CONTACT + ADDRESS
            // (UNCHANGED — YOUR ORIGINAL LOGIC BELOW)
            // ------------------------------------------------------------

            y = pager.y
            val leftColX = pager.contentLeft
            var leftY = y

            val contactLabelPaint = Paint(typo.label).apply {
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }

            if (data.ownerPhone.isNotBlank() || data.ownerEmail.isNotBlank()) {
                c.drawText("Contact:", leftColX, leftY, contactLabelPaint)
                leftY += 16f

                val boldLabel = Paint(typo.label).apply { typeface = Typeface.DEFAULT_BOLD }
                val normalText = typo.text

                if (data.ownerPhone.isNotBlank()) {
                    val label = "Phone: "
                    c.drawText(label, leftColX, leftY, boldLabel)
                    c.drawText(
                        data.ownerPhone,
                        leftColX + boldLabel.measureText(label),
                        leftY,
                        normalText
                    )
                    leftY += 16f
                }

                if (data.ownerEmail.isNotBlank()) {
                    val label = "Email: "
                    c.drawText(label, leftColX, leftY, boldLabel)
                    c.drawText(
                        data.ownerEmail,
                        leftColX + boldLabel.measureText(label),
                        leftY,
                        normalText
                    )
                    leftY += 16f
                }
            }

            val rightColX = pager.contentLeft + pager.contentWidth
            var rightY = y

            val addressLabelPaint = Paint(typo.label).apply {
                textAlign = Paint.Align.RIGHT
                typeface = Typeface.DEFAULT_BOLD
            }
            val addressPaint = Paint(typo.label).apply { textAlign = Paint.Align.RIGHT }

            c.drawText("Address:", rightColX, rightY, addressLabelPaint)
            rightY += 16f

            listOf(
                "7, Shlok Height, Opp. Dev Paradise",
                "& Dharti Silver, Nr. Mansarovar Road,",
                "Chandkheda, Ahmedabad, Gujarat - 382424"
            ).forEach {
                c.drawText(it, rightColX, rightY, addressPaint)
                rightY += 16f
            }

            y = maxOf(leftY, rightY)
            pager.y = y
            pager.space(8f)
            pager.lineAcross(rules.faintLine)
            pager.space(12f)

            // ------------------------------------------------------------
            // 7) BIG "Invoice" TITLE
            // ------------------------------------------------------------
            val invoiceTitlePaint = Paint(typo.title).apply {
                textAlign = Paint.Align.CENTER
                textSize = 24f
            }
            val invoiceTitleY = pager.y + 22f
            c.drawText("Invoice", centerX, invoiceTitleY, invoiceTitlePaint)
            pager.y = invoiceTitleY + 20f
            pager.space(6f)

            // ------------------------------------------------------------
            // 8) CUSTOMER + INVOICE INFO
            // ------------------------------------------------------------
            val boldLabel2 = Paint(typo.label).apply { typeface = Typeface.DEFAULT_BOLD }
            val normalText2 = typo.text
            val infoRowHeight = 18f
            val leftX = pager.contentLeft
            val rightX = pager.contentLeft + pager.contentWidth
            var infoY = pager.y

            val rightLabel = Paint(boldLabel2).apply { textAlign = Paint.Align.RIGHT }
            val rightValue = Paint(normalText2).apply { textAlign = Paint.Align.RIGHT }

            // customer
            c.drawText("Customer: ", leftX, infoY, boldLabel2)
            c.drawText(
                data.customerName,
                leftX + boldLabel2.measureText("Customer: "),
                infoY,
                normalText2
            )

            // invoice #
            val invoiceFileName =
                CustomerDetailsConstants.getInvoiceFileName(data.orderId, data.invoiceNumber)

            val invoiceValueWidth = rightValue.measureText(invoiceFileName)
            val invoiceLabelX = rightX - invoiceValueWidth - 4f

            c.drawText("Invoice #: ", invoiceLabelX, infoY, rightLabel)
            c.drawText(invoiceFileName, rightX, infoY, rightValue)

            infoY += infoRowHeight

            // customer phone
            c.drawText("Phone: ", leftX, infoY, boldLabel2)
            c.drawText(
                data.customerPhone,
                leftX + boldLabel2.measureText("Phone: "),
                infoY,
                normalText2
            )

            // date
            val dateValue = data.occurredAtText
            val dateValueWidth = rightValue.measureText(dateValue)
            val dateLabelX = rightX - dateValueWidth - 4f

            c.drawText("Invoice Generated: ", dateLabelX, infoY, rightLabel)
            c.drawText(dateValue, rightX, infoY, rightValue)

            infoY += infoRowHeight
            pager.y = infoY
            pager.space(10f)
            pager.space(8f)
        }
    }

    private object ItemsSection {
        // Updated alignment: Item = LEFT, others = CENTER
        private val spec = TableSpec(
            listOf(
                ColumnSpec(title = "Item",       widthFraction = 0.42f, align = Align.LEFT,   padRight = 8f),
                ColumnSpec(title = "Type",       widthFraction = 0.16f, align = Align.CENTER),
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
            val singleRowHeight = 18f
            val twoLineRowHeight = 28f

            data.items.forEach { item ->
                val owner = item.owner.takeIf { it.isNotBlank() }
                val hasOwner = (owner != null) && (owner != item.name)

                val rowHeight = if (hasOwner) twoLineRowHeight else singleRowHeight

                // Page break handling (we know the row height)
                pager.ensure(rowHeight + 8f) {
                    HeaderSection.draw(pager, data, typo, rules)
                    drawHeader(pager, typo, rules)
                }

                val unit = money.format(item.unitPrice)
                val disc = if (item.discount > 0.0) "${item.discount}%" else "-"
                val total = money.format(item.total)

                if (hasOwner) {
                    // 2-line cell: description + "Owner: ..."
                    table.itemRowWithSubtext(
                        title = item.description,
                        subtext = "Owner: $owner",
                        otherCells = listOf(
                            item.productType,
                            unit,
                            disc,
                            total
                        ),
                        rowHeight = twoLineRowHeight
                    )
                } else {
                    // Single-line row, ellipsized if too long
                    table.ellipsizedRow(
                        listOf(item.description, item.productType, unit, disc, total),
                        rowHeight = singleRowHeight,
                        descriptionPaint = typo.descriptionSmall // new smaller font
                    )
                }
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

    private object TermsSection {

        fun drawBoxed(
            pager: Pager,
            terms: List<String>,
            typo: Typography,
            rules: Rules
        ) {
            val c = pager.canvas

            val paddingTop = 18f
            val paddingBottom = 12f
            val paddingLeft = 16f
            val paddingRight = 16f
            val bulletIndent = 20f
            val lineSpacing = 6f

            val headingPaint = Paint(typo.title).apply {
                textSize = 13.5f
                textAlign = Paint.Align.LEFT
            }

            val bulletPaint = Paint(typo.text).apply {
                textSize = 10f
                textAlign = Paint.Align.LEFT
            }

            val textPaint = Paint(typo.text).apply {
                textSize = 10f
                textAlign = Paint.Align.LEFT
            }

            // -------------------------------
            // 1. Compute height REQUIRED
            // -------------------------------
            val maxTextWidth = pager.contentWidth - paddingLeft - paddingRight - bulletIndent
            var requiredHeight = paddingTop + paddingBottom + 22f + 20f   // heading + heading gap

            terms.forEachIndexed { index, term ->
                val lines = wrapText(term, textPaint, maxTextWidth)

                // First bullet line
                requiredHeight += 16f

                // Wrapped lines
                requiredHeight += (lines.size - 1) * 16f

                // Spacing between items except last
                if (index != terms.lastIndex) requiredHeight += lineSpacing
            }

            // -------------------------------
            // 2. Ensure page space for box
            // -------------------------------
            pager.ensure(requiredHeight + 10f)

            // -------------------------------
            // 3. Draw box FIRST (fixed height)
            // -------------------------------
            val boxLeft = pager.contentLeft
            val boxRight = pager.contentLeft + pager.contentWidth
            val boxTop = pager.y
            val boxBottom = pager.y + requiredHeight

            c.drawRect(
                boxLeft,
                boxTop,
                boxRight,
                boxBottom,
                rules.tableBorder
            )

            // -------------------------------
            // 4. Draw content INSIDE fixed box
            // -------------------------------
            var yCursor = boxTop + paddingTop

            // Heading
            c.drawText("Terms & Conditions", boxLeft + paddingLeft, yCursor, headingPaint)
            yCursor += 20f + 20f

            // Terms
            terms.forEachIndexed { index, term ->
                val lines = wrapText(term, textPaint, maxTextWidth)

                // First line with bullet
                c.drawText("• ${lines[0]}", boxLeft + paddingLeft, yCursor, bulletPaint)
                yCursor += 16f

                // Additional lines
                lines.drop(1).forEach { ln ->
                    c.drawText(ln, boxLeft + paddingLeft + bulletIndent, yCursor, textPaint)
                    yCursor += 16f
                }

                // Spacing between items (except last)
                if (index != terms.lastIndex) yCursor += lineSpacing
            }

            // -------------------------------
            // 5. Move pager.y BELOW box
            // -------------------------------
            pager.y = boxBottom + 10f
        }
    }

    private object ProductsChipsSection {

        fun drawChips(pager: Pager, items: List<String>, typo: Typography) {
            val c = pager.canvas
            val chipPaddingX = 12f
            val chipPaddingY = 6f
            val chipSpacing = 8f

            val textPaint = Paint(typo.text).apply {
                textSize = 10.5f
                color = Color.BLACK
                textAlign = Paint.Align.LEFT
            }

            // Light Gray Chip Background
            val chipPaint = Paint().apply {
                color = Color.rgb(235, 235, 235)   // << light gray chips
                style = Paint.Style.FILL
            }

            val centerX = pager.contentLeft + pager.contentWidth / 2f

            val chipWidths = items.map { item ->
                val textWidth = textPaint.measureText(item)
                textWidth + chipPaddingX * 2
            }

            val totalWidth = chipWidths.sum() + chipSpacing * (chipWidths.size - 1)

            var x = centerX - totalWidth / 2f
            var y = pager.y

            chipWidths.forEachIndexed { i, chipW ->
                val chipH = textPaint.textSize + chipPaddingY * 2
                val top = y
                val bottom = y + chipH

                c.drawRoundRect(x, top, x + chipW, bottom, 12f, 12f, chipPaint)

                val textY =
                    top + chipH / 2 - (textPaint.descent() + textPaint.ascent()) / 2

                c.drawText(items[i], x + chipPaddingX, textY, textPaint)

                x += chipW + chipSpacing
            }

            pager.y += (textPaint.textSize + chipPaddingY * 2) + 14f
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

private const val COLUMN_GUTTER = 24f

private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var line = ""

    for (word in words) {
        val test = if (line.isEmpty()) word else "$line $word"
        if (paint.measureText(test) <= maxWidth) {
            line = test
        } else {
            lines += line
            line = word
        }
    }
    if (line.isNotEmpty()) lines += line

    return lines
}
