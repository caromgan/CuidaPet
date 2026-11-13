package com.carolinagandin.cuidadog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlimentacionHistorialDao {
    @Query("SELECT * FROM alimentacion_historial WHERE mascotaId = :mascotaId ORDER BY fecha DESC, id DESC")
    fun getHistorialPorMascota(mascotaId: Long): Flow<List<AlimentacionHistorialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorial(historial: AlimentacionHistorialEntity): Long
}
