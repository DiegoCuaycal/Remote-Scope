package com.diegocuaycal.monitordispositivoapp.network

import android.content.Context
import android.util.Log
import com.diegocuaycal.monitordispositivoapp.data.AppDatabase
import com.diegocuaycal.monitordispositivoapp.sensors.DeviceStatusHelper
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.runBlocking

class APIServer(private val context: Context) : NanoHTTPD(8080) {

    override fun serve(session: IHTTPSession): Response {
        return if (!isAuthenticated(session)) {
            newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Unauthorized")
        } else {
            when {
                session.uri.startsWith("/api/sensor_data") -> handleSensorData(session)
                session.uri.startsWith("/api/device_status") -> handleDeviceStatus()
                else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
            }
        }
    }

    private fun isAuthenticated(session: IHTTPSession): Boolean {
        val headers = session.headers
        val tokenRecibido = headers["authorization"]?.trim()
        val usuarioRecibido = headers["usuario"]?.trim()
        val contrasenaRecibida = headers["contrasena"]?.trim()

        val db = AppDatabase.getDatabase(context)
        val dao = db.credencialDao()

        val credencial = runBlocking {
            dao.obtenerCredencial()
        }

        val tokenValido = credencial?.token?.trim()
        val usuarioValido = credencial?.usuario?.trim()
        val contrasenaValida = credencial?.contrasena?.trim()

        val accesoPorToken = tokenRecibido != null && tokenRecibido == tokenValido
        val accesoPorCredenciales = usuarioRecibido == usuarioValido && contrasenaRecibida == contrasenaValida

        Log.d("APIServer", "Autenticación: Token=$tokenRecibido, Usuario=$usuarioRecibido")

        return accesoPorToken || accesoPorCredenciales
    }


    private fun handleSensorData(session: IHTTPSession): Response {
        val params = session.parameters
        val startTime = params["start_time"]?.firstOrNull()?.toLongOrNull()
        val endTime = params["end_time"]?.firstOrNull()?.toLongOrNull()

        if (startTime == null || endTime == null) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing start_time or end_time")
        }

        val dao = AppDatabase.getDatabase(context).gpsDataDao()
        val dataList = dao.getDataBetweenTimestamps(startTime, endTime)

        val json = dataList.joinToString(separator = ",", prefix = "[", postfix = "]") { it.toString() }

        return newFixedLengthResponse(Response.Status.OK, "application/json", json)
    }

    private fun handleDeviceStatus(): Response {
        val statusJson = DeviceStatusHelper.getStatus(context).toString()

        Log.d("APIServer", "Estado del dispositivo enviado: $statusJson")

        return newFixedLengthResponse(Response.Status.OK, "application/json", statusJson)
    }
}

