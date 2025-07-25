package com.diegocuaycal.monitordispositivoapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.diegocuaycal.monitordispositivoapp.data.AppDatabase
import com.diegocuaycal.monitordispositivoapp.data.ConfiguracionGPS
import com.diegocuaycal.monitordispositivoapp.data.Credencial
import com.diegocuaycal.monitordispositivoapp.network.APIServer
import com.diegocuaycal.monitordispositivoapp.sensors.DeviceStatusHelper
import com.diegocuaycal.monitordispositivoapp.sensors.GPSManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var gpsManager: GPSManager
    private lateinit var apiServer: APIServer
    private var recolectando = false

    // UI
    private lateinit var estadoRecoleccion: TextView
    private lateinit var botonToggle: Button
    private lateinit var textInfoDispositivo: TextView
    private lateinit var textIP: TextView

    // Handler para actualizar estado
    private val handler = Handler(Looper.getMainLooper())
    private val estadoUpdater = object : Runnable {
        override fun run() {
            mostrarInfoDispositivo()
            handler.postDelayed(this, 10000) // cada 10 segundos
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        estadoRecoleccion = findViewById(R.id.textEstadoRecoleccion)
        botonToggle = findViewById(R.id.btnToggleRecoleccion)
        textInfoDispositivo = findViewById(R.id.textInfoDispositivo)
        textIP = findViewById(R.id.textIP)

        gpsManager = GPSManager(this)

        configurarRecoleccionGPS()
        configurarCredencialesAPI()
        solicitarPermisosUbicacion()
        mostrarIPLocal()

        // Listener botón
        botonToggle.setOnClickListener {
            if (recolectando) {
                gpsManager.stopLocationUpdates()
                handler.removeCallbacks(estadoUpdater)
                recolectando = false
                estadoRecoleccion.text = "Estado: Detenido"
                botonToggle.text = "Iniciar Recolección"
            } else {
                gpsManager.startLocationUpdates()
                handler.post(estadoUpdater)
                recolectando = true
                estadoRecoleccion.text = "Estado: En Recolección"
                botonToggle.text = "Detener Recolección"
            }
        }

        // Iniciar servidor API
        apiServer = APIServer(this)
        apiServer.start()
        android.util.Log.d("MainActivity", "Servidor HTTP iniciado en el puerto 8080")

        mostrarInfoDispositivo()
    }

    private fun mostrarIPLocal() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        val ip = String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
        textIP.text = "IP Local: $ip:8080"
    }

    private fun mostrarInfoDispositivo() {
        val info = DeviceStatusHelper.getStatus(this).toString(4)
        textInfoDispositivo.text = info
    }

    private fun solicitarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            gpsManager.startLocationUpdates()
            handler.post(estadoUpdater)
            recolectando = true
            estadoRecoleccion.text = "Estado: En Recolección"
            botonToggle.text = "Detener Recolección"
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            gpsManager.startLocationUpdates()
            handler.post(estadoUpdater)
            recolectando = true
            estadoRecoleccion.text = "Estado: En Recolección"
            botonToggle.text = "Detener Recolección"
        } else {
            estadoRecoleccion.text = "Estado: Permiso Denegado"
        }
    }

    private fun configurarRecoleccionGPS() {
        val db = AppDatabase.getDatabase(this)
        val dao = db.configuracionGPSDao()

        val configuracion = ConfiguracionGPS(
            id = 1,
            diasHabilitados = "2,3,4,5,6", // Lunes a Viernes
            horaInicio = 8,
            horaFin = 22
        )

        GlobalScope.launch {
            dao.insertarConfiguracion(configuracion)
        }
    }

    private fun configurarCredencialesAPI() {
        val db = AppDatabase.getDatabase(this)
        val dao = db.credencialDao()

        val credencial = Credencial(
            token = "mi_token_secreto_123",
            usuario = "admin",
            contrasena = "1234"
        )

        GlobalScope.launch {
            dao.insertar(credencial)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsManager.stopLocationUpdates()
        apiServer.stop()
        handler.removeCallbacks(estadoUpdater)
    }
}


