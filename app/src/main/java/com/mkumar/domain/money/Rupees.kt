// com.mkumar.domain.money.Rupees.kt
package com.mkumar.domain.money

@JvmInline
value class Rupees(val value: Long) {
    operator fun plus(o: Rupees) = Rupees(value + o.value)
    operator fun minus(o: Rupees) = Rupees(value - o.value)
    operator fun times(qty: Int) = Rupees(value * qty)
    fun atLeastZero() = Rupees(value.coerceAtLeast(0))
    companion object { val ZERO = Rupees(0) }
}
