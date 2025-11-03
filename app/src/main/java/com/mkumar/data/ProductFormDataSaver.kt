package com.mkumar.data

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// Simple JSON-based Saver
val ProductFormDataSaver: Saver<ProductFormData, String> = Saver(
    save = { form -> Json.encodeToString(form) },
    restore = { json -> Json.decodeFromString<ProductFormData>(json) }
)
