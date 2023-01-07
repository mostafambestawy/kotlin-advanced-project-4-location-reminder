package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivityViewModel
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.REQUEST_ENABLE_MY_LOCATION
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import kotlinx.android.synthetic.main.activity_reminders.*
import org.koin.android.ext.android.inject
import kotlin.properties.Delegates

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 2
private const val REQUEST_BUILD_GEOFENCING_REQUEST = 300
const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
const val GEOFENCE_RADIUS_IN_METERS = 100f
const val GEOFENCE_EXPIRATION_IN_MILLISECONDS = 24 * 60 * 60 * 1000L

//for testing make GEOFENCE_LOITERING_DELAY_IN_MILLISECONDS low value
const val GEOFENCE_LOITERING_DELAY_IN_MILLISECONDS = 1000


/**
 * The RemindersActivity that holds the reminders fragments
 */

class RemindersActivity : AppCompatActivity() {
    private lateinit var authenticationActivityViewModel: AuthenticationActivityViewModel

    val saveReminderViewModel: SaveReminderViewModel by inject()
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    var askBackGroundPermissionStep = false
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private lateinit var geofencingClient: GeofencingClient
    private var testing by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        authenticationActivityViewModel =
            ViewModelProvider(this)[AuthenticationActivityViewModel::class.java]


        geofencingClient = LocationServices.getGeofencingClient(this)

        testing = (applicationContext as MyApp).testing


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

            confirmDialog(getString(R.string.location_permisssions_required)) {
                launchLocationSettingsIntent()
            }

        } else {
            when (requestCode) {
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
                    SelectLocationFragment().map.isMyLocationEnabled = true
                }
                REQUEST_BUILD_GEOFENCING_REQUEST -> {
                    addGeoFencingRequest()
                }
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

    fun checkPermissionsAddGeofenceRequest() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            addGeoFencingRequest()
        } else {
            requestForegroundAndBackgroundLocationPermissions(REQUEST_BUILD_GEOFENCING_REQUEST)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeoFencingRequest() {
        val reminderData = saveReminderViewModel.savedReminder!!
        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(
                reminderData.latitude!!,
                reminderData.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            //  best practices for geofencing
            //  Use the dwell transition type to reduce alert spam
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_DWELL
            )
            .setLoiteringDelay(GEOFENCE_LOITERING_DELAY_IN_MILLISECONDS)
            .build()


        //geofenceList.add(geofence)
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_DWELL)
            .addGeofences(listOf(geofence))
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {

                saveReminderViewModel.savedReminder = null
                Snackbar.make(
                    findViewById(R.id.nav_host_fragment),
                    "Geofencing added for the reminder",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            addOnFailureListener {

                if ((it.message != null)) {
                    Log.w("TAG", it.message!!)
                    Snackbar.make(
                        findViewById(R.id.nav_host_fragment),
                        it.message!!,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        }
    }

    @TargetApi(29)
    fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }


    @TargetApi(29)
    fun requestForegroundAndBackgroundLocationPermissions(requestCode: Int) {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        Log.d("TAG", "Request foreground only location permission")

        /**Any Android apps targeting API 30 are now no longer allowed to ask for BACKGROUND_PERMISSION at the same time as regular location permission. You have to split it into 2 seperate asks:

        Ask for regular foreground location permission, once granted,
        Ask for BACKGROUND_LOCATION permission on a new ask
        https://stackoverflow.com/questions/69102394/permissions-dialog-not-showing-in-android-11
         **/

        if (runningQOrLater) askBackGroundPermissionStep = true;

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            requestCode
        )

    }

    fun confirmDialog(message: String, action: () -> Unit) {
        if(testing) {
            action()
        }
        else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    // Delete selected note from database
                    action()
                }
                .setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    private fun launchLocationSettingsIntent() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
        // TODO: use navigation
     }

}
