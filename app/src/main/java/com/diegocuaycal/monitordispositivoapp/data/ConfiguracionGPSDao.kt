package com.diegocuaycal.monitordispositivoapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConfiguracionGPSDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarConfiguracion(configuracion: ConfiguracionGPS)

    @Query("SELECT * FROM configuracion_gps WHERE id = 1")
    suspend fun obtenerConfiguracion(): ConfiguracionGPS?
}
