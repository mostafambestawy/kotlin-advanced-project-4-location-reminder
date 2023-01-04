package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

const val ACTION_GEOFENCE_EVENT ="ACTION_GEOFENCE_EVENT"
const val GEOFENCE_RADIUS_IN_METERS = 100f
const val GEOFENCE_EXPIRATION_IN_MILLISECONDS = 24*60*60*1000L
class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(activity as RemindersActivity, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(activity as RemindersActivity, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        geofencingClient = LocationServices.getGeofencingClient(activity as RemindersActivity)
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            //if (viewModel.geofenceIsActive()) return
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                (activity as RemindersActivity).checkDeviceLocationSettingsAndNavigateToSelectLocation()
            } else {
                requestForegroundAndBackgroundLocationPermissions()
            }


        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value


//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            _viewModel.validateAndSaveReminder(
                ReminderDataItem(
                    title,
                    description,
                    location,
                    latitude,
                    longitude
                )
            )


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }


    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        Log.d("TAG", "Request foreground only location permission")

        /**Any Android apps targeting API 30 are now no longer allowed to ask for BACKGROUND_PERMISSION at the same time as regular location permission. You have to split it into 2 seperate asks:

        Ask for regular foreground location permission, once granted,
        Ask for BACKGROUND_LOCATION permission on a new ask
        https://stackoverflow.com/questions/69102394/permissions-dialog-not-showing-in-android-11
         **/

        if (runningQOrLater) (activity as RemindersActivity).askBackGroundPermissionStep = true;
        // resultCode do not care
        ActivityCompat.requestPermissions(
            activity as RemindersActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            0
        )

    }
    private fun addGeofenceRequest(reminderData: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(
                reminderData.latitude!!,
                reminderData.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        if (ActivityCompat.checkSelfPermission(
                activity as RemindersActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {

                Log.e("Add Geofence", geofence.requestId)

            }
            addOnFailureListener {

                if ((it.message != null)) {
                    Log.w("TAG", it.message!!)
                }
            }

        }

    }


}
