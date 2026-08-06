package com.together.app.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.util.Locale

object NetworkUtils {
    private const val TAG = "NetworkUtils"

    fun getLocalIpAddress(context: Context): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            
            if (ipAddress == 0) {
                Log.w(TAG, "IP address is 0, Wi-Fi might not be connected")
                return null
            }

            // Correctly handle byte order for formatted IP string
            val formattedIp = String.format(
                Locale.US,
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
            
            Log.d(TAG, "Detected Host IP: $formattedIp (Raw: $ipAddress)")
            return formattedIp
        } catch (e: Exception) {
            Log.e(TAG, "Networking error: Failed to get local IP", e)
            return null
        }
    }
}
