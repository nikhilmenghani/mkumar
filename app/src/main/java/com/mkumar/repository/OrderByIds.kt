package com.mkumar.repository

fun <T> List<T>.orderByIds(ids: List<String>, idSelector: (T) -> String): List<T> {
    val pos = ids.withIndex().associate { it.value to it.index }
    return this.sortedBy { pos[idSelector(it)] ?: Int.MAX_VALUE }
}
