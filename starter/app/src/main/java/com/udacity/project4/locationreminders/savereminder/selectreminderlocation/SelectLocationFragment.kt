package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.math.RoundingMode
import java.text.DecimalFormat


const val REQUEST_ENABLE_MY_LOCATION = 10

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {


    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    lateinit var map: GoogleMap
    private var selectedLatLng: LatLng? = null
    private var selectedPoi: PointOfInterest? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODODONE: add the map setup implementation
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


//        TODODONE: zoom to the user location after taking his permission

//        TODODONE: add style to the map
//        TODODONE: put a marker to location that the user selected


//        TODODONE: call this function after the user confirms on the selected location

        binding.submitLocationButton.setOnClickListener {
            if (selectedLatLng == null) {
                _viewModel.showSnackBar.value = getString(R.string.tap_to_select_location)
            } else {
                confirmDialog("Sure With the Selected Location") { onLocationSelected() }
            }
        }



        return binding.root
    }

    private fun navigateToSaveLocationFragment() {
        _viewModel.navigationCommand.value =
            NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
    }

    private fun onLocationSelected() {
        //        TODODONE: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.latitude.value = selectedLatLng?.latitude
        _viewModel.longitude.value = selectedLatLng?.longitude
        _viewModel.selectedPOI.value = selectedPoi
        if (selectedPoi != null) _viewModel.reminderSelectedLocationStr.value = selectedPoi?.name
        else _viewModel.reminderSelectedLocationStr.value =
            StringBuilder().append(roundDouble(selectedLatLng?.latitude)).append(",")
                .append(roundDouble(selectedLatLng?.longitude)).toString()
        navigateToSaveLocationFragment()

    }

    private fun roundDouble(double: Double?):String{
        val decimalFormat = DecimalFormat("#.######")
        decimalFormat.roundingMode =  RoundingMode.DOWN
        return decimalFormat.format(double)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        enableMyLocation()
        zoomToCurrentLocationIfConfirmed()
        setMapClick(map)
        setMapStyle(map)
        setPoiClick(map)


    }

    private fun isFineLocationPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        (activity as RemindersActivity),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isFineLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                (activity as RemindersActivity),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ENABLE_MY_LOCATION
            )
        }
    }

    private fun zoomToCurrentLocationIfConfirmed() {
        confirmDialog("Do you  you want to zoom current Location ?") { zoomToCurrentLocation() }
    }

    private fun confirmDialog(message: String, action: () -> Unit) {
        val builder = AlertDialog.Builder(activity)
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

    private fun zoomToCurrentLocation() {
        if (map.myLocation != null) {
            val zoomLevel = 15f
            val location = map.myLocation
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude,
                    ), zoomLevel
                )
            )
        }
    }

    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
            )
            selectedLatLng = latLng

        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    activity as RemindersActivity,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("TAG", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("TAG", "Can't find style. Error: ", e)
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            selectedLatLng = poi.latLng
            selectedPoi = poi
            map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )?.showInfoWindow()
        }
    }

}
