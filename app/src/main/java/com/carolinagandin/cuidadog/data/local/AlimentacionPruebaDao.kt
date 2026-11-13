package com.carolinagandin.cuidadog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlimentacionPruebaDao {
    @Query("SELECT * FROM alimentacion_pruebas WHERE mascotaId = :mascotaId ORDER BY id DESC")
    fun getPruebasPorMascota(mascotaId: Long): Flow<List<AlimentacionPruebaEntity>>

    @Query("SELECT * FROM alimentacion_pruebas WHERE mascotaId = :mascotaId AND estado = 'En curso' ORDER BY id DESC")
    fun getPruebasEnCurso(mascotaId: Long): Flow<List<AlimentacionPruebaEntity>>

    @Query("SELECT * FROM alimentacion_pruebas WHERE mascotaId = :mascotaId ORDER BY id DESC LIMIT 1")
    fun getUltimaPrueba(mascotaId: Long): Flow<AlimentacionPruebaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrueba(prueba: AlimentacionPruebaEntity): Long

    @Update
    suspend fun updatePrueba(prueba: AlimentacionPruebaEntity)
}
