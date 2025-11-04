// com.mkumar.domain.pricing.PricingService.kt
package com.mkumar.domain.pricing

interface PricingService {
    fun price(input: PricingInput): PricingResult
}
