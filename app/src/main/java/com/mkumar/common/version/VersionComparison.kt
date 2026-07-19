package com.mkumar.common.version

data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<SemanticVersion> {
    override fun compareTo(other: SemanticVersion): Int =
        compareValuesBy(this, other, SemanticVersion::major, SemanticVersion::minor, SemanticVersion::patch)

    companion object {
        fun parse(value: String): SemanticVersion? {
            val normalized = value.trim()
                .removePrefix("dev-v")
                .removePrefix("v")
                .removeSuffix("-debug")
            val parts = normalized.split('.')
            if (parts.size !in 2..3) return null
            return SemanticVersion(
                major = parts[0].toIntOrNull() ?: return null,
                minor = parts[1].toIntOrNull() ?: return null,
                patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
            )
        }
    }
}

fun isVersionNewer(remote: String, installed: String): Boolean {
    val remoteVersion = SemanticVersion.parse(remote) ?: return false
    val installedVersion = SemanticVersion.parse(installed) ?: return false
    return remoteVersion > installedVersion
}
