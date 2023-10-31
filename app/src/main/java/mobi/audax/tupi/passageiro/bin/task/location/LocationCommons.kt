package mobi.audax.tupi.passageiro.bin.task.location

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import mobi.audax.tupi.passageiro.R
import org.jetbrains.annotations.Contract

class LocationCommons {

    fun isLocationEnabled(context: Context) : Boolean {
        val locationManager =  context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @Contract("_, null -> true")
    fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            return true
        }
        val timeDistance = 1000 * 60 * 2

        val timeDelta: Long = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > timeDistance
        val isSignificantlyOlder: Boolean = timeDelta < -timeDistance
        val isNewer = timeDelta > 0
        if (isSignificantlyNewer) {
            return true
        } else if (isSignificantlyOlder) {
            return false
        }
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200
        val isFromSameProvider = isSameProvider(location.provider, currentBestLocation.provider)
        return if (isMoreAccurate) {
            true
        } else if (isNewer && !isLessAccurate) {
            true
        } else isNewer && !isSignificantlyLessAccurate && isFromSameProvider
    }

    @Contract(value = "null, null -> true; null, !null -> false", pure = true)
    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else provider1 == provider2
    }

    fun isMock(context: Context, location: Location): Boolean {
        if (context.resources.getBoolean(R.bool.debug)) {
            return false;
        }

        val isMock = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            location.isFromMockProvider
        } else {
            location.isMock
        }

        if (isMock) {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 1000L, 1000L), 2))
            Toast.makeText(context, R.string.mock_location_not_allowed, Toast.LENGTH_LONG).show()
        }
        return isMock
    }

}