package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivityViewModel
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.REQUEST_ENABLE_MY_LOCATION
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import kotlinx.android.synthetic.main.activity_reminders.*

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 2



/**
 * The RemindersActivity that holds the reminders fragments
 */

class RemindersActivity : AppCompatActivity() {
    private lateinit var authenticationActivityViewModel: AuthenticationActivityViewModel
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    var askBackGroundPermissionStep = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        authenticationActivityViewModel =
            ViewModelProvider(this)[AuthenticationActivityViewModel::class.java]


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("TAG", "ActivityOnRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED

        ) {
            // Permission denied.
            Log.d("TAG", getString(R.string.permission_denied_explanation))
        } else {
            when(requestCode) {
                0 -> {
                    if (runningQOrLater && askBackGroundPermissionStep) {
                        //requestCode don not care
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            0
                        )
                        askBackGroundPermissionStep = false
                    } else checkDeviceLocationSettingsAndNavigateToSelectLocation()
                }

                REQUEST_ENABLE_MY_LOCATION -> {
                    SelectLocationFragment().map.isMyLocationEnabled = true }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun checkDeviceLocationSettingsAndNavigateToSelectLocation(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {

                try {

                    exception.startResolutionForResult(
                        this,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Log.d("TAG", getString(R.string.location_required_error))
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                navigateToSelectLocation()
            }
        }
    }

    private fun navigateToSelectLocation() {
        findNavController(R.id.nav_host_fragment).navigate(R.id.selectLocationFragment)
    }

}
