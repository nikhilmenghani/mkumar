package com.mkumar.ui.screens.customer.model

import androidx.compose.runtime.Immutable
import com.mkumar.data.ProductFormData
import java.time.Instant

@Immutable
data class CustomerHeaderUi(
    val id: String,
    val name: String,
    val phoneFormatted: String,
    val joinedAt: Instant?,
    val totalOrders: Int,
    val totalSpent: Int, // rupees minor units
)

@Immutable
data class OrderRowUi(
    val id: String,
    val occurredAt: Instant,
    val itemsLabel: String,
    val amount: Int,
    val isQueued: Boolean,     // not yet synced
    val isSynced: Boolean,     // successfully synced
    val hasInvoice: Boolean,
)

@Immutable
data class OrderFilterUi(
    val query: String = "",
    val sortNewestFirst: Boolean = true,
)


// New Order creation flow (bottom sheet)
//@Immutable
//data class NewOrderUi(
//    val selectedType: ProductType? = null,
//    val lens: LensFormState = LensFormState(),
//    val frame: FrameFormState = FrameFormState(),
//    val contactLens: ContactLensFormState = ContactLensFormState(),
//    val canSave: Boolean = false,
//    val saving: Boolean = false,
//)




