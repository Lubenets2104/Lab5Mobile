package com.example.flightsearch.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_preferences")

class SearchPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val SEARCH_QUERY_KEY = stringPreferencesKey("search_query")
    }

    val searchQuery: Flow<String> = dataStore.data.map { preferences ->
        preferences[SEARCH_QUERY_KEY] ?: ""
    }

    suspend fun saveSearchQuery(query: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_QUERY_KEY] = query
        }
    }
}
