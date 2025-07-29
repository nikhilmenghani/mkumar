package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BottomSheetViewModel : ViewModel() {

    sealed interface SheetState {
        data object AddCustomer : SheetState
        data object RemoveCustomer : SheetState
    }

    private val mSheetStateFlow: MutableStateFlow<SheetState> =
        MutableStateFlow(SheetState.AddCustomer)

    // publicly accessible SheetState
    val sheetStateFlow = mSheetStateFlow.asStateFlow()

    // to update SheetState
    fun updateState(sheetState: SheetState) {
        mSheetStateFlow.value = sheetState
    }

    fun resetState() {
        mSheetStateFlow.value = SheetState.AddCustomer
    }
}