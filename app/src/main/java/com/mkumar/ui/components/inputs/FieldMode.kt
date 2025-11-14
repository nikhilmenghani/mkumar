package com.mkumar.ui.components.inputs

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import java.math.RoundingMode
import java.util.Locale

/* ---------- FieldMode: one prop to rule formatting + keyboard ---------- */

sealed interface FieldMode {
    val keyboardType: KeyboardType
    val defaultIme: ImeAction

    /** Return the new string to write back on commit; return input as-is to keep it. */
    fun formatOnCommit(input: String): String

    /** Override to coerce while typing (optional, usually passthrough). */
    fun sanitizeOnChange(input: String): String = input

    /* ---- Presets ---- */
    data object PlainText : FieldMode {
        override val keyboardType = KeyboardType.Text
        override val defaultIme = ImeAction.Next
        override fun formatOnCommit(input: String) = input.trim()
    }

    data class Phone(
        val prefixCountryCode: Boolean = false   // set true for +91 autostart
    ) : FieldMode {

        override val keyboardType = KeyboardType.Phone
        override val defaultIme = ImeAction.Next

        override fun sanitizeOnChange(input: String): String {
            // Allow only digits
            val digits = input.filter { it.isDigit() }

            return if (prefixCountryCode) {
                when {
                    digits.startsWith("91") -> "+$digits"       // user typed 91…
                    digits.startsWith("0") -> "+91" + digits.drop(1)
                    digits.isNotEmpty() -> "+91$digits"
                    else -> ""
                }
            } else {
                digits
            }
        }

        override fun formatOnCommit(input: String): String {
            // Standardize on digits only at commit time
            val digits = input.filter { it.isDigit() }

            return if (prefixCountryCode) {
                when {
                    digits.startsWith("91") -> "+$digits"
                    digits.length >= 10 -> "+91$digits"
                    digits.isNotEmpty() -> "+91$digits"
                    else -> ""
                }
            } else {
                digits
            }
        }
    }

    /** e.g. SPH/CYL/ADD: +1.00, -0.50 (scale padded), optional + on positive */
    data class SignedDecimal(
        val scale: Int = 2,
        val forcePlus: Boolean = true
    ) : FieldMode {
        override val keyboardType = KeyboardType.Decimal
        override val defaultIme = ImeAction.Next
        override fun formatOnCommit(input: String): String {
            val t = input.trim().replace(',', '.') // tolerate comma
            val n = t.toBigDecimalOrNull() ?: return t
            val scaled = n.setScale(scale, RoundingMode.HALF_UP)
            val abs = scaled.abs().toPlainString()
            return when {
                scaled.signum() < 0 -> "-$abs"
                forcePlus -> "+$abs"
                else -> abs
            }
        }
    }

    /** e.g. Axis: clamp 0..180, integers only */
    data object AxisDegrees : FieldMode {
        override val keyboardType = KeyboardType.Number
        override val defaultIme = ImeAction.Next
        override fun sanitizeOnChange(input: String): String =
            input.filter { it.isDigit() } // keep typing simple; no +/- or dots

        override fun formatOnCommit(input: String): String {
            val n = input.filter { it.isDigit() }.toIntOrNull() ?: return input.trim()
            val clamped = n.coerceIn(0, 180)
            return clamped.toString()
        }
    }

    /** Whole-number quantities (>=1). */
    data object IntegerPositive : FieldMode {
        override val keyboardType = KeyboardType.Number
        override val defaultIme = ImeAction.Next
        override fun sanitizeOnChange(input: String) = input.filter { it.isDigit() }
        override fun formatOnCommit(input: String): String {
            val n = input.filter { it.isDigit() }.toIntOrNull() ?: 1
            return n.coerceAtLeast(1).toString()
        }
    }

    /** Percent 0..100, no % sign stored. */
    data object Percent0to100 : FieldMode {
        override val keyboardType = KeyboardType.Number
        override val defaultIme = ImeAction.Next
        override fun sanitizeOnChange(input: String) = input.filter { it.isDigit() }
        override fun formatOnCommit(input: String): String {
            val n = input.filter { it.isDigit() }.toIntOrNull() ?: 0
            return n.coerceIn(0, 100).toString()
        }
    }

    /** NEW: TitleCase for human names/owners: "john DOE-smith" -> "John Doe-Smith" */
    data class TitleCase(
        val locale: Locale = Locale.getDefault()
    ) : FieldMode {
        override val keyboardType = KeyboardType.Text
        override val defaultIme = ImeAction.Next

        override fun sanitizeOnChange(input: String): String {
            // collapse repeated spaces while typing (optional nice touch)
            return input.replace(Regex("\\s+"), " ")
        }

        override fun formatOnCommit(input: String): String = input.toTitleCasePreservingDelimiters(locale)
    }

    /** NEW: lowerCamelCase: "product owner name" -> "productOwnerName" */
    data class LowerCamelCase(
        val locale: Locale = Locale.getDefault()
    ) : FieldMode {
        override val keyboardType = KeyboardType.Text
        override val defaultIme = ImeAction.Next

        override fun sanitizeOnChange(input: String): String {
            // keep simple; users can type freely
            return input
        }

        override fun formatOnCommit(input: String): String = input.toLowerCamel(locale)
    }
}

private fun String.toTitleCasePreservingDelimiters(locale: Locale): String {
    if (isBlank()) return this
    val lower = lowercase(locale)
    val breakers = setOf(' ', '-', '\'', '’', '/', '\t', '\n')
    val out = StringBuilder(lower.length)
    var newWord = true
    for (ch in lower) {
        if (newWord && ch.isLetter()) {
            out.append(ch.titlecase(locale))
            newWord = false
        } else {
            out.append(ch)
            newWord = ch in breakers
        }
    }
    return out.toString()
}

private fun String.toLowerCamel(locale: Locale): String {
    if (isBlank()) return this
    val parts = trim().split(Regex("[\\s_\\-/'’]+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return ""
    val first = parts.first().lowercase(locale)
    val rest = parts.drop(1).joinToString("") { w ->
        w.lowercase(locale).replaceFirstChar { it.titlecase(locale) }
    }
    return first + rest
}