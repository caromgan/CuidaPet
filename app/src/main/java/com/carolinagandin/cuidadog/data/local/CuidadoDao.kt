package com.carolinagandin.cuidadog.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CuidadoDao {
    @Query("SELECT * FROM cuidados WHERE mascotaId = :mascotaId AND fecha = :fecha ORDER BY hora ASC, id ASC")
    fun getCuidadosPorFecha(mascotaId: Long, fecha: String): Flow<List<CuidadoEntity>>

    @Query("SELECT * FROM cuidados WHERE mascotaId = :mascotaId AND fecha >= :fechaManana AND fecha <= :fechaLimite ORDER BY fecha ASC, hora ASC, id ASC")
    fun getCuidadosProximos10Dias(mascotaId: Long, fechaManana: String, fechaLimite: String): Flow<List<CuidadoEntity>>

    @Query("SELECT * FROM cuidados WHERE mascotaId = :mascotaId ORDER BY fecha ASC, hora ASC, id ASC")
    fun getTodosLosCuidados(mascotaId: Long): Flow<List<CuidadoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuidado(cuidado: CuidadoEntity): Long

    @Update
    suspend fun updateCuidado(cuidado: CuidadoEntity)

    @Delete
    suspend fun deleteCuidado(cuidado: CuidadoEntity)

    @Query("DELETE FROM cuidados WHERE mascotaId = :mascotaId AND tipo = 'Alimentación' AND titulo = :titulo AND fecha >= :fechaHoy AND realizado = 0")
    suspend fun eliminarCuidadosPendientesFuturos(mascotaId: Long, titulo: String, fechaHoy: String)
}
