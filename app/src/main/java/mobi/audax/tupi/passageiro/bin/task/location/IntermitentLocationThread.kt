package mobi.audax.tupi.passageiro.bin.task.location;

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import mobi.audax.tupi.passageiro.bin.task.GeoDecodeTask
import mobi.audax.tupi.passageiro.bin.util.Prefs


class IntermitentLocationThread(val context: Context, val onLocationUpdate: (location: Location?) -> Unit) : LocationListener {

    private var UPDATE_INTERVAL = (1000 * 10).toLong()  // 10 segundos de intervalo
    private val MAX_WAIT_TIME = UPDATE_INTERVAL * 2  // 20 segundos
    private var bestLocation: Location? = null

    fun requestLocation() {
        this.locationService()
    }

    private fun locationService() {
        val locationEnabled = LocationCommons().isLocationEnabled(context)
        val locationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (locationEnabled && locationPermission) {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
            locationRequest.fastestInterval = 1000
            locationRequest.interval = UPDATE_INTERVAL
            locationRequest.maxWaitTime = MAX_WAIT_TIME
            locationRequest.smallestDisplacement = 15f
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location -> onLocationChanged(location) }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { onLocationChanged(it) }
                }
            }, Looper.myLooper()!!)
        }
    }

    override fun onLocationChanged(location: Location) {
        try {
            if (location != null) {
                val commons = LocationCommons()
                if (!commons.isMock(context, location) && commons.isBetterLocation(location, bestLocation)) {
                    Log.v("IntermitentLocationThread", "isBetter true")
                    val prefs = Prefs(context)
                    prefs.latitude = location.latitude.toFloat()
                    prefs.longitude = location.longitude.toFloat()
                    prefs.precisao = location.accuracy
                    prefs.velocidade = location.speed * 3.6f
                    prefs.bearing = location.bearing
                    location.extras?.let {
                        if (it.containsKey("satellites")) {
                            prefs.satellites = it.getInt("satellites")
                        }
                    }


                    GeoDecodeTask(context, location).decoder { }
                    bestLocation = location

                    onLocationUpdate(bestLocation)
                } else {
                    Log.v("IntermitentLocationThread", "isBetter false")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}