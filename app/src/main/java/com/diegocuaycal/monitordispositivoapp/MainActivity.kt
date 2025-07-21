package com.diegocuaycal.monitordispositivoapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.diegocuaycal.monitordispositivoapp.sensors.GPSManager
import com.diegocuaycal.monitordispositivoapp.data.AppDatabase
import com.diegocuaycal.monitordispositivoapp.data.ConfiguracionGPS
import com.diegocuaycal.monitordispositivoapp.network.APIServer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var gpsManager: GPSManager
    private lateinit var apiServer: APIServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gpsManager = GPSManager(this)
        configurarRecoleccionGPS()
        solicitarPermisosUbicacion()

        // Iniciar servidor API
        apiServer = APIServer(this)
        apiServer.start()
        android.util.Log.d("MainActivity", "Servidor HTTP iniciado en el puerto 8080")
    }

    private fun solicitarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            gpsManager.startLocationUpdates()
            android.util.Log.d("MainActivity", "Permiso ya concedido. Iniciando GPSManager")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            gpsManager.startLocationUpdates()
            android.util.Log.d("MainActivity", "Permiso concedido en tiempo de ejecución. Iniciando GPSManager")
        } else {
            android.util.Log.d("MainActivity", "Permiso de ubicación denegado por el usuario.")
        }
    }

    private fun configurarRecoleccionGPS() {
        val db = AppDatabase.getDatabase(this)
        val dao = db.configuracionGPSDao()

        val configuracion = ConfiguracionGPS(
            id = 1,
            diasHabilitados = "2,3,4,5,6", // Lunes a Viernes
            horaInicio = 8, // 8 AM
            horaFin = 22    // 10 PM
        )

        GlobalScope.launch {
            dao.insertarConfiguracion(configuracion)
            android.util.Log.d("MainActivity", "Configuración de recolección GPS guardada: $configuracion")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsManager.stopLocationUpdates()
        apiServer.stop()
        android.util.Log.d("MainActivity", "Servidor HTTP detenido")
    }
}

