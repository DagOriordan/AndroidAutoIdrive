package me.hufman.androidautoidrive

import android.content.Context
import me.hufman.androidautoidrive.carapp.music.KJUMultimedia
import me.hufman.androidautoidrive.carapp.music.MusicApp
import me.hufman.androidautoidrive.carapp.music.MusicAppMode
import me.hufman.androidautoidrive.music.MusicAppDiscovery
import me.hufman.androidautoidrive.music.MusicController
import me.hufman.idriveconnectionkit.android.security.SecurityAccess

class MusicService(val context: Context, val securityAccess: SecurityAccess) {
	var threadMusic: CarThread? = null
	var carappMusic: MusicApp? = null

	fun start(): Boolean {
		synchronized(this) {
			if (threadMusic == null) {
				threadMusic = CarThread("Music") {
					val handler = threadMusic?.handler ?: return@CarThread
					val musicAppDiscovery = MusicAppDiscovery(context, handler)
					val musicController = MusicController(context, handler)
					val kjuMultimedia = KJUMultimedia(context, GraphicsHelpersAndroid(), "com.spotify.music")
					carappMusic = MusicApp(securityAccess,
							CarAppAssetManager(context, "multimedia"),
							PhoneAppResourcesAndroid(context),
							GraphicsHelpersAndroid(),
							musicAppDiscovery,
							musicController,
							kjuMultimedia,
							MusicAppMode(MutableAppSettings(context)))
					musicAppDiscovery.discoverApps()
				}
				threadMusic?.start()
			}
		}
		return true
	}

	fun stop() {
		val handler = threadMusic?.handler
		handler?.post {
			carappMusic?.musicController?.disconnectApp(pause=false)
			carappMusic?.musicAppDiscovery?.cancelDiscovery()
			carappMusic = null

			handler.looper?.quitSafely()
		}
		threadMusic = null
	}



}