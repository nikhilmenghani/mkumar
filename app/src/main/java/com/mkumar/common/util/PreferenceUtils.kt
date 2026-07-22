package com.mkumar.common.util

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.mkumar.App
import com.mkumar.data.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

inline fun <reified A> dataStoreMutableState(
    keyName: String,
    defaultValue: A,
    getPreferencesKey: (keyName: String) -> Preferences.Key<A>,
): MutableState<A> {
    val key: Preferences.Key<A> = getPreferencesKey(keyName)
    // Compose Preview's layoutlib does not run App.onCreate(), so the application
    // context is unavailable while preview-only preference objects are initialized.
    // Use the preference's declared default in that environment; the real app still
    // reads DataStore normally after App.onCreate has installed the context.
    val initialValue = runCatching {
        runBlocking {
            App.globalClass.dataStore.data.first()[key] ?: defaultValue
        }
    }.getOrDefault(defaultValue)
    val snapshotMutableState: MutableState<A> = mutableStateOf(initialValue)

    return object : MutableState<A> {
        override var value: A
            get() = snapshotMutableState.value
            set(value) {
                val rollbackValue = snapshotMutableState.value
                snapshotMutableState.value = value
                runBlocking {
                    try {
                        App.globalClass.dataStore.edit {
                            if (value != null) {
                                it[key] = value as A
                            } else {
                                it.remove(key)
                            }
                        }
                    } catch (e: Exception) {
                        snapshotMutableState.value = rollbackValue
                    }
                }
            }

        override fun component1() = value
        override fun component2(): (A) -> Unit = { value = it }
    }
}
