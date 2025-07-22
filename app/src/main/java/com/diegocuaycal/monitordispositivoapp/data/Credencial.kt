package com.diegocuaycal.monitordispositivoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credenciales")
data class Credencial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val token: String,
    val usuario: String,
    val contrasena: String
)
