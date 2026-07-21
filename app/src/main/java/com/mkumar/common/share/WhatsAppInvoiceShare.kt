package com.mkumar.common.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppInvoiceShare {
    private val packages = listOf("com.whatsapp", "com.whatsapp.w4b")

    fun share(context: Context, pdfUri: Uri, rawPhone: String): Result {
        val phone = normalizeIndianNumber(rawPhone) ?: return Result.InvalidPhone
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra("jid", "$phone@s.whatsapp.net")
            clipData = ClipData.newRawUri("invoice", pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val targetPackage = packages.firstOrNull { packageName ->
            intent.setPackage(packageName)
            intent.resolveActivity(context.packageManager) != null
        } ?: return Result.NotInstalled

        return runCatching {
            context.startActivity(intent.setPackage(targetPackage))
            Result.Started
        }.getOrElse { Result.Failed(it.message) }
    }

    internal fun normalizeIndianNumber(rawPhone: String): String? {
        var digits = rawPhone.filter(Char::isDigit)
        if (digits.startsWith("00")) digits = digits.drop(2)
        if (digits.length == 11 && digits.startsWith("0")) digits = digits.drop(1)
        if (digits.length == 10) digits = "91$digits"
        return digits.takeIf { it.length == 12 && it.startsWith("91") }
    }

    sealed interface Result {
        data object Started : Result
        data object InvalidPhone : Result
        data object NotInstalled : Result
        data class Failed(val reason: String?) : Result
    }
}
