package com.diegocuaycal.monitordispositivoapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GPSDataDao {

    @Insert
    suspend fun insert(gpsData: GPSData)

    @Query("SELECT * FROM gps_data ORDER BY timestamp DESC")
    suspend fun getAll(): List<GPSData>

    @Query("SELECT * FROM gps_data WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getDataBetweenTimestamps(startTime: Long, endTime: Long): List<GPSData>

}
