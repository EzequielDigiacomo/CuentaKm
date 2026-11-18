package com.cuentakm.app.data.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.cuentakm.app.domain.model.VehicleSummary
import com.cuentakm.app.ui.pdf.SummaryPdf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Escribe el PDF en cache y lo comparte con la hoja del sistema.
 * Mismo nombre de archivo que la web: `cuentakm-{patente}-{aaaa-mm-dd}.pdf`.
 */
@Singleton
class PdfExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun export(summary: VehicleSummary): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val file = File(dir, "cuentakm-${summary.patente}-${LocalDate.now()}.pdf")

        FileOutputStream(file).use { out ->
            SummaryPdf.write(summary, out)
        }

        file
    }

    fun shareIntent(file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "CuentaKm · ${file.nameWithoutExtension}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(send, "Compartir PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
