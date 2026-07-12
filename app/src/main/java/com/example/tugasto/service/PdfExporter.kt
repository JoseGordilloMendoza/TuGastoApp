package com.example.tugasto.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.tugasto.ui.screens.TransactionDisplay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40f
    private val BLUE = Color.parseColor("#1D4ED8")
    private val BLUE_LIGHT = Color.parseColor("#EFF6FF")
    private val GRAY = Color.parseColor("#6B7280")
    private val DARK = Color.parseColor("#111827")
    private val GREEN = Color.parseColor("#15803D")
    private val GREEN_BG = Color.parseColor("#DCFCE7")
    private val DIVIDER = Color.parseColor("#E5E7EB")

    fun generate(context: Context, transactions: List<TransactionDisplay>): Uri? {
        return try {
            val document = PdfDocument()
            var pageNum = 1
            var info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNum).create()
            var page = document.startPage(info)
            var cv = page.canvas
            var y = MARGIN

            fun newPage() {
                document.finishPage(page)
                pageNum++
                info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNum).create()
                page = document.startPage(info)
                cv = page.canvas
                y = MARGIN + 20f
            }

            fun checkNewPage(neededHeight: Float) {
                if (y + neededHeight > PAGE_H - MARGIN) newPage()
            }

            val contentW = PAGE_W - 2 * MARGIN

            // ── Header azul ───────────────────────────────────────────────────
            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = BLUE; style = Paint.Style.FILL
            }
            cv.drawRoundRect(RectF(MARGIN, y, PAGE_W - MARGIN, y + 70f), 10f, 10f, headerPaint)

            val titleP = paint(22f, Color.WHITE, bold = true)
            cv.drawText("TuGasto — Reporte de Gastos", MARGIN + 16f, y + 28f, titleP)

            val monthLabel = SimpleDateFormat("MMMM yyyy", Locale("es", "PE"))
                .format(Date()).replaceFirstChar { it.uppercaseChar() }
            cv.drawText(monthLabel, MARGIN + 16f, y + 50f, paint(13f, Color.parseColor("#BFDBFE")))
            y += 82f

            // ── Total + conteo ────────────────────────────────────────────────
            val total = transactions.sumOf { it.amount }
            val boxH = 52f
            val boxW = (contentW - 12f) / 2f

            drawCard(cv, MARGIN, y, boxW, boxH, BLUE_LIGHT)
            cv.drawText("Total gastado", MARGIN + 12f, y + 18f, paint(10f, BLUE))
            cv.drawText("S/ ${String.format(Locale.US, "%.2f", total)}", MARGIN + 12f, y + 38f, paint(18f, BLUE, bold = true))

            drawCard(cv, MARGIN + boxW + 12f, y, boxW, boxH, BLUE_LIGHT)
            cv.drawText("Transacciones", MARGIN + boxW + 24f, y + 18f, paint(10f, BLUE))
            cv.drawText("${transactions.size}", MARGIN + boxW + 24f, y + 38f, paint(18f, BLUE, bold = true))
            y += boxH + 20f

            // ── Resumen por categoría ─────────────────────────────────────────
            checkNewPage(100f)
            cv.drawText("RESUMEN POR CATEGORÍA", MARGIN, y + 12f, paint(9f, GRAY, letterSpacing = 0.1f))
            y += 22f

            val grouped = transactions.groupBy { it.categoryName }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .entries.sortedByDescending { it.value }

            grouped.forEach { (catName, catTotal) ->
                checkNewPage(28f)
                cv.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint(0.5f, DIVIDER))
                y += 9f
                cv.drawText(catName, MARGIN + 4f, y + 11f, paint(11f, DARK, bold = true))
                val pct = if (total > 0) (catTotal / total * 100).toInt() else 0

                // barra de progreso
                val barX = MARGIN + 130f
                val barW = contentW - 130f - 80f
                val trackP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = DIVIDER }
                cv.drawRoundRect(RectF(barX, y + 3f, barX + barW, y + 13f), 4f, 4f, trackP)
                val fillP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BLUE }
                cv.drawRoundRect(RectF(barX, y + 3f, barX + barW * pct / 100f, y + 13f), 4f, 4f, fillP)

                cv.drawText("S/ ${String.format(Locale.US, "%.2f", catTotal)}", PAGE_W - MARGIN - 4f, y + 11f,
                    paint(11f, DARK, bold = true, align = Paint.Align.RIGHT))
                y += 22f
            }

            // ── Lista de transacciones ────────────────────────────────────────
            y += 8f
            checkNewPage(30f)
            cv.drawText("DETALLE DE TRANSACCIONES", MARGIN, y + 12f, paint(9f, GRAY, letterSpacing = 0.1f))
            y += 22f

            val dateF = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            transactions.forEach { tx ->
                checkNewPage(26f)
                cv.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint(0.5f, DIVIDER))
                y += 8f

                val dateStr = dateF.format(Date(tx.timestamp))
                cv.drawText(dateStr, MARGIN + 4f, y + 10f, paint(9f, GRAY))
                cv.drawText(tx.description, MARGIN + 54f, y + 10f, paint(10f, DARK))
                cv.drawText(tx.categoryName, MARGIN + 54f, y + 21f, paint(8f, GRAY))
                cv.drawText("S/ ${String.format(Locale.US, "%.2f", tx.amount)}", PAGE_W - MARGIN - 4f, y + 10f,
                    paint(10f, DARK, bold = true, align = Paint.Align.RIGHT))
                y += 28f
            }

            // ── Total final ───────────────────────────────────────────────────
            checkNewPage(40f)
            y += 4f
            drawCard(cv, MARGIN, y, contentW, 36f, GREEN_BG)
            cv.drawText("TOTAL", MARGIN + 12f, y + 22f, paint(11f, GREEN, bold = true))
            cv.drawText("S/ ${String.format(Locale.US, "%.2f", total)}", PAGE_W - MARGIN - 12f, y + 22f,
                paint(13f, GREEN, bold = true, align = Paint.Align.RIGHT))
            y += 50f

            // ── Pie de página ─────────────────────────────────────────────────
            val footerText = "Generado por TuGasto · ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}"
            cv.drawText(footerText, PAGE_W / 2f, PAGE_H - 20f, paint(8f, GRAY, align = Paint.Align.CENTER))

            document.finishPage(page)

            val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val file = File(dir, "reporte_tugasto_${System.currentTimeMillis()}.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()

            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawCard(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 8f, 8f, p)
    }

    private fun paint(
        size: Float,
        color: Int = DARK,
        bold: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT,
        letterSpacing: Float = 0f
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size * 2.2f
        this.color = color
        typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        textAlign = align
        this.letterSpacing = letterSpacing
    }
}
