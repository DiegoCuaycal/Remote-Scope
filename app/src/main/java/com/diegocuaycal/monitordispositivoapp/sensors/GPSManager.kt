package com.diegocuaycal.monitordispositivoapp.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.diegocuaycal.monitordispositivoapp.data.AppDatabase
import com.diegocuaycal.monitordispositivoapp.data.GPSData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GPSManager(private val context: Context) : LocationListener {

    private var locationManager: LocationManager? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        Log.d("GPSManager", "Iniciando actualizaciones de ubicación")

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                10000L,
                5f,
                this
            )
            Log.d("GPSManager", "Listener de ubicación registrado correctamente")
        } else {
            Log.e("GPSManager", "Permiso de ubicación no concedido")
        }
    }

    fun stopLocationUpdates() {
        locationManager?.removeUpdates(this)
        Log.d("GPSManager", "Ubicación detenida")
    }

    override fun onLocationChanged(location: Location) {
        try {
            Log.d("GPSManager", "Se recibió una nueva ubicación")

            val latitude = location.latitude
            val longitude = location.longitude
            val timestamp = System.currentTimeMillis()

            // Obtención del Device ID compatible con Android 10+
            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )

            // Log completo
            Log.d("GPSManager", "Lat: $latitude, Long: $longitude, Timestamp: $timestamp, DeviceID: $deviceId")

            if (!GPSConfig.estaDentroDelHorario(context)) {
                Log.d("GPSManager", "Ubicación descartada por fuera de horario permitido")
                return
            }

            val gpsData = GPSData(
                deviceId = deviceId,
                latitude = latitude,
                longitude = longitude,
                timestamp = timestamp
            )

            val db = AppDatabase.getDatabase(context)
            val dao = db.gpsDataDao()

            GlobalScope.launch {
                dao.insert(gpsData)
                Log.d("GPSManager", "Ubicación guardada en la base de datos")
            }
        } catch (e: Exception) {
            Log.e("GPSManager", "Error al procesar ubicación: ${e.message}", e)
        }
    }



    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
