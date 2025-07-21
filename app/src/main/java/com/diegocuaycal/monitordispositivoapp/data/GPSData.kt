package com.diegocuaycal.monitordispositivoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_data")
data class GPSData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val provider: String,
    val accuracy: Float,
    val altitude: Double,
    val speed: Float
)
