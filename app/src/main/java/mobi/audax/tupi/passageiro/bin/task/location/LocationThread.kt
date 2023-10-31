package mobi.audax.tupi.passageiro.bin.task.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import mobi.audax.tupi.passageiro.bin.util.Prefs

class LocationThread(val context: Context, val onLocationUpdate: (location: Location?) -> Unit) : LocationListener {

    private var bestLocation: Location? = null

    var useLastKnowLocation = true
    var useOnlyLastKnowLocation = false

    fun requestLocation() {
        this.locationService()
    }

    private fun locationService() {
        val locationEnabled = LocationCommons().isLocationEnabled(context)
        val locationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (locationEnabled && locationPermission) {
            try {
                val locationRequest = LocationRequest.create()
                locationRequest.interval = 0
                locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
                locationRequest.fastestInterval = 1000
                locationRequest.numUpdates = 1
                val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
                if (useLastKnowLocation || useOnlyLastKnowLocation) {
                    try {
                        if (context is Activity) {
                            fusedLocationProviderClient.lastLocation.addOnSuccessListener(context) { location -> location?.let { onLocationChanged(it) } }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (!useOnlyLastKnowLocation) {
                    Looper.myLooper()?.apply {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                locationResult.lastLocation?.let { onLocationChanged(it) }
                            }
                        }, this)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        try {
            if (location != null) {
                val commons = LocationCommons()
                if (!commons.isMock(context, location) && commons.isBetterLocation(location, bestLocation)) {
                    Prefs(context).recordLocation(location)
                    bestLocation = location
                }
                onLocationUpdate(bestLocation)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}