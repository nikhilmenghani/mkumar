package com.mkumar.ui.meta

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lens
import androidx.compose.material.icons.outlined.ProductionQuantityLimits
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.ui.graphics.vector.ImageVector
import com.mkumar.viewmodel.ProductType

data class ProductTypeMeta(
    val displayName: String,
    val icon: ImageVector,
    val tags: List<String> = emptyList(),
    val category: String? = null,
    val disabled: Boolean = false,
)

// Provide meta for the known types. Add more as your enum grows.
val productTypeMeta: Map<ProductType, ProductTypeMeta> = mapOf(
    ProductType.Frame to ProductTypeMeta("Frame", Icons.Outlined.RemoveRedEye, tags = listOf("frames", "spectacles")),
    ProductType.Glass to ProductTypeMeta("Glass", Icons.Outlined.Lens, tags = listOf("lenses", "optical")),
    ProductType.Lens to ProductTypeMeta("Contact Lens", Icons.Outlined.Lens, tags = listOf("lens", "contacts")),
    ProductType.GeneralProduct to ProductTypeMeta("General Product", Icons.Outlined.ProductionQuantityLimits, tags = listOf("Watch", "Chain", "Belt"))
)
