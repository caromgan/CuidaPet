package com.carolinagandin.cuidadog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PerroDao {
    @Query("SELECT * FROM perro ORDER BY id ASC")
    fun getTodasLasMascotas(): Flow<List<PerroEntity>>

    @Query("SELECT * FROM perro WHERE id = :id LIMIT 1")
    fun getMascotaPorId(id: Long): Flow<PerroEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMascota(perro: PerroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerro(perro: PerroEntity): Long

    @Update
    suspend fun updateMascota(perro: PerroEntity)

    @Query("UPDATE perro SET fotoUri = :fotoUri WHERE id = :id")
    suspend fun updateFotoUri(id: Long, fotoUri: String?)
}
