package com.ymy.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ymy.core.Ktx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created on 5/6/21 18:14.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

val Context.dataStore by preferencesDataStore(name = "user")

object DataStoreHelper {
    suspend fun saveToDataStore(key: Preferences.Key<String>, stringValue: String) {
        Ktx.app.dataStore.edit { user ->
            user[key] = stringValue
        }
    }

    suspend fun getFromDataStore(key: Preferences.Key<String>): String {
        val nameFlow: Flow<String> = Ktx.app.dataStore.data.map { user ->
            user[key] ?: ""
        }
        return nameFlow.first()
    }
}
