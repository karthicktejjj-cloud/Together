package com.together.app.network.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class NsdAdvertiser(context: Context) : TogetherNsdManager(context) {

    private var registrationListener: NsdManager.RegistrationListener? = null
    var serviceName: String? = null

    fun registerService(port: Int, roomCode: String, hostName: String, roomName: String) {
        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = "Together_${roomCode}"
            this.serviceType = SERVICE_TYPE
            this.setPort(port)
            setAttribute("roomCode", roomCode)
            setAttribute("hostName", hostName)
            setAttribute("roomName", roomName)
            setAttribute("deviceName", android.os.Build.MODEL)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                serviceName = info.serviceName
                Log.d(TAG, "Service registered: ${info.serviceName}")
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Registration failed: $errorCode")
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {
                Log.d(TAG, "Service unregistered: ${info.serviceName}")
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Unregistration failed: $errorCode")
            }
        }

        nsdManager.registerService(
            serviceInfo,
            NsdManager.PROTOCOL_DNS_SD,
            registrationListener
        )
    }

    fun unregisterService() {
        registrationListener?.let {
            try {
                nsdManager.unregisterService(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering service", e)
            } finally {
                registrationListener = null
            }
        }
    }
}
