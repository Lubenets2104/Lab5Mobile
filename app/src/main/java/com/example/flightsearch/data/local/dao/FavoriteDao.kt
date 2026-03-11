package com.example.flightsearch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flightsearch.data.local.entity.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Query(
        """
        SELECT * FROM favorite
        WHERE departure_code = :departureCode AND destination_code = :destinationCode
        LIMIT 1
        """
    )
    fun getFavorite(departureCode: String, destinationCode: String): Flow<Favorite?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: Favorite)

    @Query(
        """
        DELETE FROM favorite
        WHERE departure_code = :departureCode AND destination_code = :destinationCode
        """
    )
    suspend fun deleteFavoriteByRoute(departureCode: String, destinationCode: String)
}
