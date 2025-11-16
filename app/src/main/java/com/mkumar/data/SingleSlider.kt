package com.mkumar.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SingleSlider {
    var show by mutableStateOf(false)
    var title = emptyString
        private set
    var sliderTitle = emptyString
        private set
    var description = emptyString
        private set
    var onConfirm: (value: Int) -> Unit = {}
        private set
    var onDismiss: () -> Unit = {}
        private set
    var value = 0
    var defaultProductHighlightIntensity = 65

    fun dismiss() {
        show = false
        title = emptyString
        sliderTitle = emptyString
        description = emptyString
        value = 0
        onConfirm = {}
        onDismiss = {}
    }

    fun show(
        title: String,
        sliderTitle: String,
        description: String,
        value: Int,
        onConfirm: (value: Int) -> Unit,
        onDismiss: () -> Unit
    ) {
        SingleSlider.title = title
        SingleSlider.sliderTitle = sliderTitle
        SingleSlider.description = description
        SingleSlider.value = value
        SingleSlider.onConfirm = onConfirm
        SingleSlider.onDismiss = onDismiss
        show = true
    }
}