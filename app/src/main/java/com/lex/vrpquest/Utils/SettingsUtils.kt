package com.lex.vrpquest.Utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.lex.vrpquest.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

fun SettingStoreString(context:Context, id:String, data:String) {
    val setting = stringSetPreferencesKey(id)
    runBlocking {
        context.dataStore.edit { settings ->
            settings[setting] = setOf(data)
        }
    }
}

fun SettingStoreStringSet(context:Context, id:String, data:Set<String>) {
    val setting = stringSetPreferencesKey(id)
    runBlocking {
        context.dataStore.edit { settings ->
            settings[setting] = data
        }
    }
}

fun SettingGetStringSet(context:Context, id:String): Set<String>? {
    val setting = stringSetPreferencesKey(id)
    var data:Set<String>? = null
    runBlocking {
        val result = context.dataStore.data.map { preferences ->
            preferences[setting]}.first()

        if (result == null) {data = null} else {
            data = result
        }
    }
    return data
}

fun SettingGetSting(context:Context, id:String): String? {
    val setting = stringSetPreferencesKey(id)
    var data:String? = null
    runBlocking {
        val result = context.dataStore.data.map { preferences ->
            preferences[setting]}.first()

        if (result == null) {data = null} else {
            data = result.first()
        }
    }
    return data
}

fun SettingStoreBoolean(context:Context, id:String, data:Boolean) {
    val setting = booleanPreferencesKey(id)
    runBlocking {
        context.dataStore.edit { settings ->
            settings[setting] = data
        }
    }
}

fun SettingGetBoolean(context:Context, id:String): Boolean? {
    val setting = booleanPreferencesKey(id)
    var data:Boolean? = null
    runBlocking {
        data = context.dataStore.data.map { preferences ->
            preferences[setting]}.first()
    }
    return data
}