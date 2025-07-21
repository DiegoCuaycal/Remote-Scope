package com.diegocuaycal.monitordispositivoapp.sensors

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import org.json.JSONObject
import java.io.File

object DeviceStatusHelper {

    fun getStatus(context: Context): JSONObject {
        val json = JSONObject()

        try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isConnected = connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true

            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            val freeStorageMB = bytesAvailable / (1024 * 1024)

            json.put("battery_level", batteryLevel)
            json.put("network_connected", isConnected)
            json.put("free_storage_mb", freeStorageMB)
            json.put("os_version", Build.VERSION.RELEASE)
            json.put("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
        } catch (e: Exception) {
            json.put("error", e.message)
        }

        return json
    }

    private class StatFs(path: String) {
        private val stat = android.os.StatFs(path)
        val blockSizeLong: Long get() = stat.blockSizeLong
        val availableBlocksLong: Long get() = stat.availableBlocksLong
    }
}
