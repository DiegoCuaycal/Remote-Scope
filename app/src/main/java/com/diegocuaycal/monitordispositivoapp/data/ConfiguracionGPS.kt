package com.diegocuaycal.monitordispositivoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion_gps")
data class ConfiguracionGPS(
    @PrimaryKey val id: Int = 1,
    val diasHabilitados: String,
    val horaInicio: Int,
    val horaFin: Int
)
