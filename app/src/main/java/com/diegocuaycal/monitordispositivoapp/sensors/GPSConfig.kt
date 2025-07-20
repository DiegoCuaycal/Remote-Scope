package com.diegocuaycal.monitordispositivoapp.sensors

import android.content.Context
import android.util.Log
import com.diegocuaycal.monitordispositivoapp.data.AppDatabase
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class GPSConfig {

    companion object {

        fun estaDentroDelHorario(context: Context): Boolean {
            val calendario = Calendar.getInstance()
            val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
            val horaActual = calendario.get(Calendar.HOUR_OF_DAY)

            var configuracionValida = false

            runBlocking {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val config = db.configuracionGPSDao().obtenerConfiguracion()

                    if (config != null) {
                        val diasHabilitados = config.diasHabilitados.split(",").mapNotNull { it.toIntOrNull() }.toSet()

                        configuracionValida = diasHabilitados.contains(diaSemana) &&
                                horaActual in config.horaInicio..config.horaFin

                        Log.d("GPSConfig", "Configuración obtenida: Dias=$diasHabilitados, HoraInicio=${config.horaInicio}, HoraFin=${config.horaFin}")
                        Log.d("GPSConfig", "Día actual: $diaSemana, Hora actual: $horaActual, ¿Dentro del horario?: $configuracionValida")
                    } else {
                        Log.d("GPSConfig", "No hay configuración registrada en la base de datos, permitiendo recolección por defecto.")
                        configuracionValida = true
                    }
                } catch (e: Exception) {
                    Log.e("GPSConfig", "Error al obtener configuración GPS: ${e.message}", e)
                    configuracionValida = true
                }
            }

            return configuracionValida
        }
    }
}

