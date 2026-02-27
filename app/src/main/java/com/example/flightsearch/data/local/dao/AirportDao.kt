package com.example.flightsearch.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.flightsearch.data.local.entity.Airport
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Query(
        """
        SELECT * FROM airport
        WHERE iata_code LIKE :query || '%' OR name LIKE '%' || :query || '%'
        ORDER BY passengers DESC
        """
    )
    fun searchAirports(query: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code = :iataCode")
    fun getAirportByCode(iataCode: String): Flow<Airport?>

    @Query("SELECT * FROM airport WHERE iata_code != :iataCode ORDER BY passengers DESC")
    fun getAllAirportsExcept(iataCode: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport ORDER BY passengers DESC")
    fun getAllAirports(): Flow<List<Airport>>
}
