package com.together.app.network.discovery

import android.content.Context
import android.net.nsd.NsdManager

abstract class TogetherNsdManager(context: Context) {
    protected val nsdManager: NsdManager =
        context.getSystemService(Context.NSD_SERVICE) as NsdManager

    companion object {
        const val SERVICE_TYPE = "_together._tcp."
        const val TAG = "TogetherNSD"
    }
}
