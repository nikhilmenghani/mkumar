package com.mkumar.common.search

import java.text.Normalizer


/** Remove diacritics and lowercase for name folding. */
fun foldName(name: String): String = Normalizer
    .normalize(name.trim(), Normalizer.Form.NFD)
    .replace("\\p{Mn}".toRegex(), "")
    .lowercase()


/** Keep digits-only for phone indexing/search. */
fun digitsOnly(phone: String?): String? = phone?.filter { it.isDigit() }?.ifBlank { null }


/**
 * Build a safe FTS5 MATCH string. We support:
 * - digits-only → phone prefix
 * - otherwise → name ALL tokens prefix, with optional phone fallback
 */
fun buildFtsMatch(raw: String): String {
    val cleaned = raw.trim()
    if (cleaned.isEmpty()) return "" // caller should guard


// Escape risky FTS chars by replacing with space
    val safe = cleaned.replace("""[\"'\-+*:?^_]""".toRegex(), " ").lowercase()


    val isDigits = safe.all { it.isDigit() }
    if (isDigits) {
        return "phone:${safe}*"
    }


    val tokens = safe.split("\\s+".toRegex()).filter { it.isNotBlank() }
    val nameAll = tokens.joinToString(" ") { tok -> "name:${tok}*" }
// Add a weak phone fallback if user typed something containing digits within alpha
    val digits = safe.filter { it.isDigit() }
    return if (digits.isNotEmpty()) "($nameAll) OR phone:${digits}*" else nameAll
}