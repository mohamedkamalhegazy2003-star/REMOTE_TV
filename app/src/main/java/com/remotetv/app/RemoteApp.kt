package com.remotetv.app

import android.app.Application

class RemoteApp : Application() {
    val controller: TvController by lazy { TvController(applicationContext) }
}
