package com.mkumar.common.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WhatsAppInvoiceShareTest {
    @Test
    fun `normalizes common Indian phone formats`() {
        assertEquals("919876543210", WhatsAppInvoiceShare.normalizeIndianNumber("98765 43210"))
        assertEquals("919876543210", WhatsAppInvoiceShare.normalizeIndianNumber("+91 98765-43210"))
        assertEquals("919876543210", WhatsAppInvoiceShare.normalizeIndianNumber("09876543210"))
        assertEquals("919876543210", WhatsAppInvoiceShare.normalizeIndianNumber("0091 9876543210"))
    }

    @Test
    fun `rejects incomplete or unsupported phone numbers`() {
        assertNull(WhatsAppInvoiceShare.normalizeIndianNumber("12345"))
        assertNull(WhatsAppInvoiceShare.normalizeIndianNumber("+1 416 555 0123"))
    }
}
