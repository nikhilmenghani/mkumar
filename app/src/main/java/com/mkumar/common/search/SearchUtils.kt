package com.mkumar.common.search

import java.text.Normalizer


// Remove diacritics and lowercase for name folding
fun foldName(input: String): String = Normalizer
    .normalize(input.trim(), Normalizer.Form.NFD)
    .replace("\\p{Mn}".toRegex(), "")
    .lowercase()


// Keep only digits for phone normalization
fun digitsOnly(input: String?): String? = input?.filter(Char::isDigit)?.ifBlank { null }


// Generic n-gram generator
fun ngrams(s: String, n: Int): List<String> {
    if (s.length < n) return emptyList()
    return (0..s.length - n).map { i -> s.substring(i, i + n) }
}


// Build FTS prefix match (FAST mode). Example: name:ni* name:ku* or phone:123*
fun buildFtsPrefixMatch(raw: String): String {
    val q = raw.trim()
    if (q.isEmpty()) return ""
    val isDigits = q.all(Char::isDigit)
    val safe = q.replace("""["'\-+*:?^_]""".toRegex(), " ").lowercase()
    return if (isDigits) {
        "phone:${safe}*"
    } else {
        val tokens = safe.split("\\s+".toRegex()).filter { it.isNotBlank() }
        tokens.joinToString(" ") { t -> "name:${t}*" }
    }
}


// Build trigram match (FLEXIBLE mode) when query length >= 3. Uses name3/phone3 FTS columns
fun buildFtsTrigramMatch(raw: String): String? {
    val q = raw.trim()
    if (q.length < 3) return null
    val isDigits = q.all(Char::isDigit)
    val folded = if (isDigits) q else foldName(q).replace(" ", "")
    val grams = ngrams(folded, 3)
    if (grams.isEmpty()) return null
    return grams.joinToString(" ") { g -> if (isDigits) "phone3:$g" else "name3:$g" }
}