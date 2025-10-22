package com.mkumar.data.local

import androidx.room.ProvidedTypeConverter
import kotlinx.serialization.json.Json
import javax.inject.Inject

@ProvidedTypeConverter
class Converters @Inject constructor(
    private val json: Json
) {

}
