package com.example.flightsearch

import android.app.Application
import com.example.flightsearch.data.datastore.SearchPreferencesRepository
import com.example.flightsearch.data.datastore.dataStore
import com.example.flightsearch.data.local.FlightDatabase
import com.example.flightsearch.data.repository.FlightRepository

class FlightSearchApplication : Application() {

    val database: FlightDatabase by lazy { FlightDatabase.getDatabase(this) }

    val flightRepository: FlightRepository by lazy {
        FlightRepository(
            airportDao = database.airportDao(),
            favoriteDao = database.favoriteDao()
        )
    }

    val searchPreferencesRepository: SearchPreferencesRepository by lazy {
        SearchPreferencesRepository(dataStore)
    }
}
