package com.diegocuaycal.monitordispositivoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GPSData::class, ConfiguracionGPS::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpsDataDao(): GPSDataDao
    abstract fun configuracionGPSDao(): ConfiguracionGPSDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monitor_dispositivo_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

