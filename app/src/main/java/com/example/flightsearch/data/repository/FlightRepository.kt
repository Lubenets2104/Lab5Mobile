package com.example.flightsearch.data.repository

import com.example.flightsearch.data.local.dao.AirportDao
import com.example.flightsearch.data.local.dao.FavoriteDao
import com.example.flightsearch.data.local.entity.Airport
import com.example.flightsearch.data.local.entity.Favorite
import kotlinx.coroutines.flow.Flow

class FlightRepository(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao
) {
    fun searchAirports(query: String): Flow<List<Airport>> =
        airportDao.searchAirports(query)

    fun getAirportByCode(iataCode: String): Flow<Airport?> =
        airportDao.getAirportByCode(iataCode)

    fun getAllAirportsExcept(iataCode: String): Flow<List<Airport>> =
        airportDao.getAllAirportsExcept(iataCode)

    fun getAllAirports(): Flow<List<Airport>> =
        airportDao.getAllAirports()

    fun getAllFavorites(): Flow<List<Favorite>> =
        favoriteDao.getAllFavorites()

    fun getFavorite(departureCode: String, destinationCode: String): Flow<Favorite?> =
        favoriteDao.getFavorite(departureCode, destinationCode)

    suspend fun addFavorite(departureCode: String, destinationCode: String) {
        favoriteDao.insertFavorite(
            Favorite(departureCode = departureCode, destinationCode = destinationCode)
        )
    }

    suspend fun removeFavorite(departureCode: String, destinationCode: String) {
        favoriteDao.deleteFavoriteByRoute(departureCode, destinationCode)
    }
}
