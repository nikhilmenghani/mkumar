package com.mkumar.ui.components.inputs

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import java.math.RoundingMode

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
}
