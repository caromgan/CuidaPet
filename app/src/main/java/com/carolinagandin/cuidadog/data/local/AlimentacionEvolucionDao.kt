package com.carolinagandin.cuidadog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlimentacionEvolucionDao {
    @Query("SELECT * FROM alimentacion_evoluciones WHERE pruebaId = :pruebaId ORDER BY id DESC")
    fun getEvolucionesPorPrueba(pruebaId: Long): Flow<List<AlimentacionEvolucionEntity>>

    @Query("SELECT * FROM alimentacion_evoluciones WHERE mascotaId = :mascotaId ORDER BY id DESC")
    fun getEvolucionesPorMascota(mascotaId: Long): Flow<List<AlimentacionEvolucionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvolucion(evolucion: AlimentacionEvolucionEntity): Long
}
