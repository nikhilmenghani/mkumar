package com.mkumar.common.version

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparisonTest {
    @Test
    fun patchTenIsNewerThanPatchNine() {
        assertTrue(isVersionNewer("0.45.10", "0.45.9-debug"))
    }

    @Test
    fun olderOrEqualVersionsAreNotUpdates() {
        assertFalse(isVersionNewer("0.45.9", "0.45.10-debug"))
        assertFalse(isVersionNewer("0.45.10", "0.45.10-debug"))
    }

    @Test
    fun tagPrefixesAreParsedSemantically() {
        assertTrue(isVersionNewer("dev-v1.0.0", "0.99.999-debug"))
    }

    @Test
    fun releaseVersionsWithoutPatchRemainSupported() {
        assertTrue(isVersionNewer("0.46", "0.45"))
        assertFalse(isVersionNewer("0.45", "0.45.0"))
    }
}
