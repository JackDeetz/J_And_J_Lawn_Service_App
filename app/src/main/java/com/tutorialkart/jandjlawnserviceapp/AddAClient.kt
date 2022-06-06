package com.tutorialkart.jandjlawnserviceapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tutorialkart.jandjlawnserviceapp.databinding.ActivityAddAClientBinding
import java.io.File
import java.io.PrintWriter

class AddAClient : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var binding: ActivityAddAClientBinding
    private var client: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var addressBookIndex : Int = 0
    private lateinit var location : Location


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddAClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (hasLocationPermission()) {
            trackLocation()
        }

        findViewById<Button>(R.id.upButton).setOnClickListener{
            if (addressBookIndex < 4)
            {
                addressBookIndex++
                updateMap(location)
                binding.addressIndexTextView.text = (addressBookIndex + 1).toString()
            }
        }
        findViewById<Button>(R.id.downButton).setOnClickListener{
            if (addressBookIndex > 0)
            {
                addressBookIndex--
                updateMap(location)
                binding.addressIndexTextView.text = (addressBookIndex + 1).toString()
            }
        }
        binding.possibleAddress.setOnClickListener{
            binding.addressEditText.setText(binding.possibleAddress.text.toString())
        }
    }


    private fun trackLocation() {

        // Create location request
        locationRequest = LocationRequest.create()
            .setInterval(15000)
            .setFastestInterval(15000)
            //priority must be high accuracy
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Create location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (locationEntry in locationResult.locations) {
                    location = locationEntry
                    updateMap(locationEntry)
                }
            }
        }

        client = LocationServices.getFusedLocationProviderClient(this)
    }

    //update map, run schedule based on trackLocation.locationRequest.interval ^^^
    private fun updateMap(location: Location) {

        // Get current location
        val currentLatLng = LatLng(
            location.latitude,
            location.longitude
        )

        val geocoder = Geocoder(this)
        val addressList =
            geocoder.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 5)
        findViewById<TextView>(R.id.possibleAddress).text = addressList[addressBookIndex].getAddressLine(0)

        // Remove previous marker
        googleMap.clear()

        // Place a marker at the current location
        val markerOptions = MarkerOptions()
            .title("Here you are!")
            .position(currentLatLng)
        googleMap.addMarker(markerOptions)

        //zoom camera
        val update: CameraUpdate =
            CameraUpdateFactory.newLatLngZoom(
                currentLatLng,
                15f)
        googleMap.animateCamera(update)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    override fun onPause() {
        super.onPause()
        client?.removeLocationUpdates(locationCallback!!)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            client?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, Looper.getMainLooper()
            )
        }
    }

    private fun hasLocationPermission(): Boolean {

        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            trackLocation()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.submit_clear_appbarr_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Determine which menu option was selected
        return when (item.itemId) {
            R.id.app_bar_action_one_submit -> {
                if (everythingIsInOrderForSubmitingJob()) {

                    //collect all data
                    val firstName = findViewById<EditText>(R.id.firstNameEditText).text.toString()
                    val lastName = findViewById<EditText>(R.id.lastNameEditText).text.toString()
                    val address = findViewById<EditText>(R.id.addressEditText).text.toString()
                    val phoneNum = findViewById<EditText>(R.id.phoneNumEditView).text.toString()
                    val jobData = prepDataForSaving(
                        firstName,
                        lastName,
                        address,
                        phoneNum
                    )
                    //log it to an internal file
                    writeToInternalFile(jobData)
                    //clear the fields
                    clearFields()
                    Toast.makeText(this, "Client successfully logged", Toast.LENGTH_LONG).show()
                }
                true
            }
            R.id.app_bar_action_two_clear_fields -> {
                //clear the fields
                clearFields()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearFields() {
        findViewById<EditText>(R.id.firstNameEditText).setText("")
        findViewById<EditText>(R.id.lastNameEditText).setText("")
        findViewById<EditText>(R.id.addressEditText).setText("")
        findViewById<EditText>(R.id.phoneNumEditView).setText("")
    }

    private fun everythingIsInOrderForSubmitingJob(): Boolean {
        val results : TextView = findViewById(R.id.newClientSuccess)
        results.text = ""
        if (findViewById<EditText>(R.id.firstNameEditText).text.toString() == "")
            results.text = "No First Name Logged\n"
        if (findViewById<EditText>(R.id.lastNameEditText).text.toString() == "")
            results.text = results.text.toString() + "No Last Name Logged\n"
        if (findViewById<EditText>(R.id.addressEditText).text.toString() == "")
            results.text = results.text.toString() + "No Address Logged\n"
        if (findViewById<EditText>(R.id.phoneNumEditView).text.toString() == "")
            results.text = results.text.toString() + "No Number Logged\n"

        if (results.text != "")
            return false
        return true

    }

    private fun prepDataForSaving(first: String, last: String, address: String, phoneNumber: String) : String {
        return first + ";" + last + "\n;" + phoneNumber + "\n;" + address + "~\n"
    }

    fun writeToInternalFile(client : String) {
        var oldData : String = ""
        val fileName = "loggedClients"
        if (File("/data/data/com.tutorialkart.jandjlawnserviceapp/files/", fileName).exists())
            oldData = readFromInternalFile()

        val outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
        val writer = PrintWriter(outputStream)

        // Write each task on a separate line
        val fileContents = client
        writer.println(oldData + fileContents)
        writer.close()
    }
    fun readFromInternalFile(): String {

        val fileName = "loggedClients"

        val inputStream = openFileInput(fileName)
        val reader = inputStream.bufferedReader()
        val stringBuilder = StringBuilder()
        val lineSeparator = System.getProperty("line.separator")

        // Append each task to stringBuilder
        reader.forEachLine { stringBuilder.append(it).append(lineSeparator) }

        return stringBuilder.toString()
    }

}