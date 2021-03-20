package uk.ekampls.mymapactivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.osmdroid.bonuspack.routing.MapQuestRoadManager
import org.osmdroid.util.GeoPoint

private const val REQUEST_CODE_LOCATION = 1001
private const val REQUEST_CODE_RESOLUTION = 1002

@Suppress("DEPRECATION")

const val KEY="BeFLXI2yThA5AZdDgmn4z5JHOkvl7YHU"
class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener {


    private lateinit var mMap: GoogleMap
    private val locationClint by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var rootManjer:MapQuestRoadManager?=null
    val list = mutableListOf<GeoPoint>()
     private val callback=object: LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (result==null) return
            val loc =result.lastLocation
            showLocation(loc)
        }
    }

    private fun showLocation(loc: Location) {
        val tashkent = LatLng(loc.latitude, loc.longitude)
        mMap.addMarker(MarkerOptions().position(tashkent).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(tashkent))

    }
    override fun onMapClick(p: LatLng?) {
        list.add(GeoPoint(p!!.latitude, p.longitude))
        mMap.addMarker(MarkerOptions().position(p))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(p))
        if (list.size>1){
            loadroting()
        }
    }
    private fun loadroting() {

    }

    private var hasEnableLocation = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        rootManjer= MapQuestRoadManager(KEY)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener (this)


        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
    }


    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!hasEnableLocation) {
                hasEnableLocation = true

                if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
                }
            }
        } else {
            checkrequestLocationAvailability()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkrequestLocationAvailability() {
        locationClint.locationAvailability
                .addOnSuccessListener {
                    if (it.isLocationAvailable) {
                        requestLocation()
                    } else {
                        setUpLocationServis()
                    }
                }
                .addOnFailureListener {
                    setUpLocationServis()
                }
    }

    private fun setUpLocationServis() {
        val request = createLocationClint()
        val settingLocation = LocationSettingsRequest.Builder()
                .addLocationRequest(request)
                .build()
        val settingClint = LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingLocation)
                .addOnSuccessListener { }
                .addOnFailureListener(this::resolveLocationExseption)

    }

    private fun createLocationClint(): LocationRequest {
        return LocationRequest().apply {
            interval = 10_000
            fastestInterval = 5_000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

    }

    private fun resolveLocationExseption(it: Exception) {
        if (it is ResolvableApiException) {
            try {
                it.startResolutionForResult(this, REQUEST_CODE_RESOLUTION)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        val req = createLocationClint()


        locationClint.requestLocationUpdates(req, callback, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_RESOLUTION) {
            requestLocation()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION &&
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkrequestLocationAvailability()
        } else {

        }
    }

    override fun onPause() {
        super.onPause()
        locationClint.removeLocationUpdates(callback)
    }


}