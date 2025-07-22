package com.diegocuaycal.monitordispositivoapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CredencialDao {

    @Insert
    suspend fun insertar(credencial: Credencial)

    @Query("SELECT * FROM credenciales LIMIT 1")
    suspend fun obtenerCredencial(): Credencial?
}
