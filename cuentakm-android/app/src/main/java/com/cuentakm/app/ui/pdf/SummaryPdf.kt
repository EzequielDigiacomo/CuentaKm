package com.cuentakm.app.ui.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.cuentakm.app.domain.model.MaintenanceSnapshot
import com.cuentakm.app.domain.model.MaintenanceStatus
import com.cuentakm.app.domain.model.MaintenanceType
import com.cuentakm.app.domain.model.VehicleSummary
import com.cuentakm.app.util.asDisplay
import com.cuentakm.app.util.km
import com.cuentakm.app.util.remainingLabel
import java.io.OutputStream

/**
 * Genera el PDF del resumen por patente replicando el que produce la web
 * (`src/utils/exportPdf.ts`): hoja A4, membrete con la marca, pie con paginado
 * y crédito, y cada bloque con su marco punteado para recortar.
 *
 * En la web el contenido se captura con html2canvas; acá se dibuja directo en
 * el canvas del PDF, así el texto sale vectorial (mejor al imprimir) y no hay
 * dependencias nuevas.
 */
object SummaryPdf {

    private const val PAGE_W = 595
    private const val PAGE_H = 842

    private const val MARGIN = 28f
    private const val HEADER_BASELINE = 34f
    private const val HEADER_RULE_Y = 44f
    private const val CONTENT_TOP = 62f
    private const val CONTENT_BOTTOM = PAGE_H - 60f
    private const val BLOCK_GAP = 14f
    private const val PAD = 12f

    private const val BRAND = "CuentaKm"
    private const val SITE_URL = "www.cuentakm.com"
    private const val CREDIT = "Desarrollado por Ezequiel Di Giacomo · Uso gratuito"

    // Paleta alineada con tokens.css (misma que usa el PDF de la web).
    private val ACCENT = Color.parseColor("#0F6B63")
    private val INK = Color.parseColor("#241C14")
    private val INK_SOFT = Color.parseColor("#5C5146")
    private val MUTED = Color.parseColor("#8A7D70")
    private val LINE = Color.parseColor("#E0D4C4")
    private val CUT = Color.parseColor("#968879")

    fun write(summary: VehicleSummary, out: OutputStream) {
        val blocks = buildBlocks(summary)
        val pages = paginate(blocks)
        val document = PdfDocument()

        try {
            pages.forEachIndexed { index, pageBlocks ->
                val info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, index + 1).create()
                val page = document.startPage(info)
                val canvas = page.canvas

                drawHeader(canvas)
                var top = CONTENT_TOP
                pageBlocks.forEach { block ->
                    block.draw(canvas, top)
                    top += block.height + BLOCK_GAP
                }
                drawFooter(canvas, index + 1, pages.size)

                document.finishPage(page)
            }
            document.writeTo(out)
        } finally {
            document.close()
        }
    }

    // ---------------------------------------------------------------- bloques

    private interface Block {
        val height: Float
        fun draw(canvas: Canvas, top: Float)
    }

    private fun buildBlocks(summary: VehicleSummary): List<Block> {
        val byType = summary.services.associateBy { it.type }
        val blocks = mutableListOf<Block>()

        blocks += PatenteBlock(summary.patente, summary.currentKm)

        val oil = byType[MaintenanceType.Oil]
        val filters = MaintenanceType.oilBundleFilters.mapNotNull { byType[it] }
        if (hasRecord(oil) || filters.any { hasRecord(it) }) {
            blocks += OilSectorBlock(oil, filters)
        }

        MaintenanceType.primary
            .filter { it != MaintenanceType.Oil }
            .mapNotNull { byType[it] }
            .filter { hasRecord(it) }
            .forEach { blocks += ServiceBlock(it.type.label, it) }

        return blocks
    }

    private fun paginate(blocks: List<Block>): List<List<Block>> {
        val pages = mutableListOf<MutableList<Block>>()
        var current = mutableListOf<Block>()
        var used = 0f
        val available = CONTENT_BOTTOM - CONTENT_TOP

        for (block in blocks) {
            val needed = if (current.isEmpty()) block.height else block.height + BLOCK_GAP
            if (current.isNotEmpty() && used + needed > available) {
                pages += current
                current = mutableListOf()
                used = 0f
            }
            if (current.isNotEmpty()) used += BLOCK_GAP
            current += block
            used += block.height
        }

        if (current.isNotEmpty()) pages += current
        return pages
    }

    private class PatenteBlock(
        private val patente: String,
        private val currentKm: Int,
    ) : Block {
        override val height: Float = PAD + 9f + 21f + 13f + PAD

        override fun draw(canvas: Canvas, top: Float) {
            drawFrame(canvas, top, top + height)
            val x = MARGIN + PAD
            var baseline = top + PAD + 9f
            canvas.drawText("Patente", x, baseline, paint(8f, MUTED))
            baseline += 21f
            canvas.drawText(patente, x, baseline, paint(18f, INK, bold = true))
            baseline += 13f
            canvas.drawText("Km actuales: ${currentKm.km()}", x, baseline, paint(9f, MUTED))
        }
    }

    private class ServiceBlock(
        private val title: String,
        private val snapshot: MaintenanceSnapshot,
    ) : Block {
        override val height: Float = PAD + 11f + 18f + 24f + 10f + 10f + PAD

        override fun draw(canvas: Canvas, top: Float) {
            drawFrame(canvas, top, top + height)
            val left = MARGIN + PAD
            val right = PAGE_W - MARGIN - PAD

            var baseline = top + PAD + 11f
            canvas.drawText(title, left, baseline, paint(12f, INK, bold = true))
            drawBadge(canvas, snapshot.status, right, baseline)

            baseline += 18f
            val colWidth = (right - left) / 2f
            drawStat(
                canvas, left, baseline,
                "Último cambio",
                snapshot.lastDoneAt?.asDisplay() ?: "—",
                snapshot.lastKm?.km() ?: "—",
            )
            drawStat(
                canvas, left + colWidth, baseline,
                "Próximo cambio",
                snapshot.nextDueAt?.asDisplay() ?: "—",
                snapshot.nextDueKm?.km() ?: "—",
            )

            baseline += 34f
            canvas.drawText(
                remainingLabel(snapshot.kmRemaining, snapshot.daysRemaining),
                left, baseline, paint(9f, INK_SOFT),
            )
        }
    }

    private class OilSectorBlock(
        private val oil: MaintenanceSnapshot?,
        private val filters: List<MaintenanceSnapshot>,
    ) : Block {

        override val height: Float
            get() {
                var h = PAD + 11f + 18f + 24f + 12f
                h += 14f
                filters.forEach { filter ->
                    h += 14f
                    if (hasRecord(filter)) h += 24f
                    h += 8f
                }
                h += PAD
                return h
            }

        override fun draw(canvas: Canvas, top: Float) {
            drawFrame(canvas, top, top + height)
            val left = MARGIN + PAD
            val right = PAGE_W - MARGIN - PAD

            var baseline = top + PAD + 11f
            canvas.drawText("Aceite", left, baseline, paint(12f, INK, bold = true))
            if (hasRecord(oil) && oil != null) {
                drawBadge(canvas, oil.status, right, baseline)
            }

            baseline += 18f
            if (hasRecord(oil) && oil != null) {
                val colWidth = (right - left) / 2f
                drawStat(
                    canvas, left, baseline,
                    "Último cambio",
                    oil.lastDoneAt?.asDisplay() ?: "—",
                    oil.lastKm?.km() ?: "—",
                )
                drawStat(
                    canvas, left + colWidth, baseline,
                    "Próximo cambio",
                    oil.nextDueAt?.asDisplay() ?: "—",
                    oil.nextDueKm?.km() ?: "—",
                )
                baseline += 34f
                canvas.drawText(
                    remainingLabel(oil.kmRemaining, oil.daysRemaining),
                    left, baseline, paint(9f, INK_SOFT),
                )
            } else {
                canvas.drawText(
                    "Todavía no hay un cambio de aceite cargado.",
                    left, baseline, paint(9f, MUTED),
                )
                baseline += 12f
            }

            baseline += 16f
            canvas.drawText(
                "Filtros en el cambio de aceite",
                left, baseline, paint(9.5f, INK_SOFT, bold = true),
            )

            MaintenanceType.oilBundleFilters.forEach { type ->
                val snapshot = filters.firstOrNull { it.type == type }
                baseline += 20f

                canvas.drawText(type.label, left, baseline, paint(9.5f, INK, bold = true))

                if (hasRecord(snapshot) && snapshot != null) {
                    drawBadge(canvas, snapshot.status, right, baseline)
                    baseline += 12f
                    canvas.drawText(
                        "Último ${snapshot.lastDoneAt?.asDisplay() ?: "—"} · " +
                            "${snapshot.lastKm?.km() ?: "—"}",
                        left, baseline, paint(8.5f, MUTED),
                    )
                    baseline += 11f
                    canvas.drawText(
                        "Próximo ${snapshot.nextDueAt?.asDisplay() ?: "—"} · " +
                            "${snapshot.nextDueKm?.km() ?: "—"}",
                        left, baseline, paint(8.5f, MUTED),
                    )
                } else {
                    val paint = paint(8.5f, MUTED)
                    canvas.drawText(
                        "No se cambió",
                        right - paint.measureText("No se cambió"),
                        baseline,
                        paint,
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------- primitivas

    private fun paint(size: Float, color: Int, bold: Boolean = false): Paint = Paint().apply {
        isAntiAlias = true
        this.color = color
        textSize = size
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun drawStat(
        canvas: Canvas,
        x: Float,
        baseline: Float,
        label: String,
        value: String,
        hint: String,
    ) {
        canvas.drawText(label, x, baseline, paint(8f, MUTED))
        canvas.drawText(value, x, baseline + 13f, paint(10.5f, INK, bold = true))
        canvas.drawText(hint, x, baseline + 24f, paint(8f, MUTED))
    }

    private fun statusForeground(status: MaintenanceStatus): Int = when (status) {
        MaintenanceStatus.Ok -> Color.parseColor("#166534")
        MaintenanceStatus.Upcoming -> Color.parseColor("#C2410C")
        MaintenanceStatus.Overdue -> Color.parseColor("#B91C1C")
        MaintenanceStatus.Unknown -> Color.parseColor("#5C5146")
    }

    private fun statusBackground(status: MaintenanceStatus): Int = when (status) {
        MaintenanceStatus.Ok -> Color.parseColor("#DCFCE7")
        MaintenanceStatus.Upcoming -> Color.parseColor("#FFEDD5")
        MaintenanceStatus.Overdue -> Color.parseColor("#FEE2E2")
        MaintenanceStatus.Unknown -> Color.parseColor("#E7E1D8")
    }

    private fun drawBadge(
        canvas: Canvas,
        status: MaintenanceStatus,
        rightEdge: Float,
        baseline: Float,
    ) {
        val labelPaint = paint(7.5f, statusForeground(status), bold = true)
        val width = labelPaint.measureText(status.label) + 12f
        val height = 14f
        val top = baseline - 11f
        val rect = RectF(rightEdge - width, top, rightEdge, top + height)

        val background = Paint().apply {
            isAntiAlias = true
            color = statusBackground(status)
        }
        canvas.drawRoundRect(rect, 7f, 7f, background)
        canvas.drawText(status.label, rect.left + 6f, rect.bottom - 4f, labelPaint)
    }

    private fun drawFrame(canvas: Canvas, top: Float, bottom: Float) {
        val border = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = CUT
            pathEffect = DashPathEffect(floatArrayOf(2.4f, 2.4f), 0f)
        }
        canvas.drawRoundRect(
            RectF(MARGIN, top, PAGE_W - MARGIN, bottom),
            8f,
            8f,
            border,
        )
    }

    private fun drawHeader(canvas: Canvas) {
        canvas.drawText(BRAND, MARGIN, HEADER_BASELINE, paint(12f, ACCENT, bold = true))

        val urlPaint = paint(9f, MUTED)
        canvas.drawText(
            SITE_URL,
            PAGE_W - MARGIN - urlPaint.measureText(SITE_URL),
            HEADER_BASELINE,
            urlPaint,
        )

        val rule = Paint().apply {
            isAntiAlias = true
            color = LINE
            strokeWidth = 0.4f
        }
        canvas.drawLine(MARGIN, HEADER_RULE_Y, PAGE_W - MARGIN, HEADER_RULE_Y, rule)
    }

    private fun drawFooter(canvas: Canvas, page: Int, pageCount: Int) {
        val ruleY = CONTENT_BOTTOM + 10f
        val rule = Paint().apply {
            isAntiAlias = true
            color = LINE
            strokeWidth = 0.4f
        }
        canvas.drawLine(MARGIN, ruleY, PAGE_W - MARGIN, ruleY, rule)

        val small = paint(8.5f, MUTED)
        canvas.drawText(SITE_URL, MARGIN, ruleY + 12f, small)

        val pageLabel = "Página $page de $pageCount"
        canvas.drawText(
            pageLabel,
            PAGE_W - MARGIN - small.measureText(pageLabel),
            ruleY + 12f,
            small,
        )

        val credit = paint(8.5f, INK_SOFT, bold = true)
        canvas.drawText(
            CREDIT,
            (PAGE_W - credit.measureText(CREDIT)) / 2f,
            ruleY + 24f,
            credit,
        )
    }

    private fun hasRecord(snapshot: MaintenanceSnapshot?): Boolean =
        snapshot != null && (snapshot.lastDoneAt != null || snapshot.lastKm != null)
}
