package com.mkumar.model

import java.util.Locale

fun provisionalInvoiceSuffix(orderId: String): String {
    val letters = orderId.filter(Char::isLetter)
        .takeLast(5)
        .uppercase(Locale.ROOT)
    return letters.padEnd(5, 'X')
}
