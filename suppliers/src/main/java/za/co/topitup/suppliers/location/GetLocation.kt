package za.co.topitup.suppliers.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

//TODO Find a better way for "context"
class GetLocation(
    fusedLocationProviderClient: FusedLocationProviderClient,
    context: Context,
) {

    private val client = fusedLocationProviderClient
    private val _context = context

    @SuppressLint("MissingPermission")
    fun fetchUpdates(): Flow<Location> = callbackFlow {
        val locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_SECS)
            fastestInterval = TimeUnit.SECONDS.toMillis(FASTEST_UPDATE_INTERVAL_SECS)
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val callBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                val userLocation = location?.let { Location(it) }

                if (userLocation != null) {
                    trySend(userLocation)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                _context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                _context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //requestPermissions()
            //return
        } else {
            client.requestLocationUpdates(locationRequest, callBack, Looper.getMainLooper())
        }
        awaitClose { client.removeLocationUpdates(callBack) }
    }


    companion object {
        private const val UPDATE_INTERVAL_SECS = 60L
        private const val FASTEST_UPDATE_INTERVAL_SECS = 5L
    }
}