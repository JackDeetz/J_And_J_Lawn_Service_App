package com.tutorialkart.jandjlawnserviceapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.io.PrintWriter
import java.time.LocalDateTime


class StartAJobActivity : AppCompatActivity() {
    @SuppressLint("NewApi")

    private var client: FusedLocationProviderClient? = null
    private var locationRequest: com.google.android.gms.location.LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var locationInfo: String = ""
    private var drivingLog : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_a_job)

        if (hasLocationPermission()) {
            trackLocation()
            setClickListeners()
        }
    }

    private fun setClickListeners() {
        //Set up active driving button listener, attached animation to run while active
        val drivingIsActiveImage: ImageView = findViewById(R.id.drivingIsActiveImageView)
        drivingIsActiveImage.setBackgroundResource(R.drawable.driving_ace_animation)
        val activeDrivingAnimation = drivingIsActiveImage.background as AnimationDrawable
        val driveButton = findViewById<Button>(R.id.startAJobDriveButton)
        val locationOutput = findViewById<TextView>(R.id.driveLastInfoTextView)

        driveButton.setOnClickListener {
            if (activeDrivingAnimation.isRunning) {
                //active driving animation stopped and hid
                drivingIsActiveImage.visibility = View.INVISIBLE
                activeDrivingAnimation.stop()
                //update button text
                driveButton.text = "Start Logging Drive"
                //update UI TextView
                locationOutput.text = locationOutput.text.toString() + "\n" + currentDateAndTime() + " -- " + locationInfo
                //update log
                drivingLog += locationOutput.text.toString() + "\n"
            } else {
                //active driving animation started
                drivingIsActiveImage.visibility = View.VISIBLE
                activeDrivingAnimation.start()
                //update button text
                driveButton.text = "Stop Logging Drive"
                //clear UI TextView
                locationOutput.text = ""
                //update UI Textview with time and location
                locationOutput.text = currentDateAndTime() + " -- " + locationInfo
            }
        }
        //Set up active driving button listener, attached animation to run while active
        val mowingIsActiveImage: ImageView = findViewById(R.id.mowingIsActiveImageView)
        mowingIsActiveImage.setBackgroundResource(R.drawable.mowing_forest_animation)
        val activeMowingAnimation = mowingIsActiveImage.background as AnimationDrawable
        val mowButton = findViewById<Button>(R.id.startMowingButton)
        val mowingTimesDisplay : TextView = findViewById(R.id.loggedMowEventsTextView)

        mowButton.setOnClickListener {
            if (activeMowingAnimation.isRunning) {
                mowingIsActiveImage.visibility = View.INVISIBLE
                activeMowingAnimation.stop()
                mowButton.text = "Start Logging Mow"
                mowingTimesDisplay.text = mowingTimesDisplay.text.toString() + "Stopped : " + currentDateAndTime("Time") + "\n"
            } else {
                mowingIsActiveImage.visibility = View.VISIBLE
                activeMowingAnimation.start()
                mowButton.text = "Stop Logging Mow"
                mowingTimesDisplay.text = mowingTimesDisplay.text.toString() + "Started : " + currentDateAndTime("Time") + " - "
            }
        }

        //Set up active driving button listener, attached animation to run while active
        val weedingIsActiveImage: ImageView = findViewById(R.id.weedEatingIsActiveImageView)
        weedingIsActiveImage.setBackgroundResource(R.drawable.weeding_animation)
        val activeWeedingAnimation = weedingIsActiveImage.background as AnimationDrawable
        val weedingButton = findViewById<Button>(R.id.startWeedEatingButton)
        val weedingTimesDisplay : TextView = findViewById(R.id.loggedWeedEatingEventsTextView)

        weedingButton.setOnClickListener {
            if (activeWeedingAnimation.isRunning) {
                weedingIsActiveImage.visibility = View.INVISIBLE
                activeWeedingAnimation.stop()
                weedingButton.text = "Start Logging Weeding"
                weedingTimesDisplay.text = weedingTimesDisplay.text.toString() + "Stopped : " + currentDateAndTime("Time") + "\n"
            } else {
                weedingIsActiveImage.visibility = View.VISIBLE
                activeWeedingAnimation.start()
                weedingButton.text = "Stop Logging Weeding"
                weedingTimesDisplay.text = weedingTimesDisplay.text.toString() + "Started : " + currentDateAndTime("Time") + " - "
            }
        }
    }

    private fun clearFields() {
        findViewById<EditText>(R.id.customerNameEditText).setText("")
        findViewById<TextView>(R.id.loggedMowEventsTextView).text = ""
        findViewById<TextView>(R.id.loggedWeedEatingEventsTextView).text = ""
        findViewById<TextView>(R.id.driveLastInfoTextView).text = ""
        findViewById<EditText>(R.id.jobNotes).setText("")
        drivingLog = ""
    }

    private fun everythingIsInOrderForSubmitingJob(): Boolean {
        val results : TextView = findViewById(R.id.submitResultsTextView)
        results.text = ""
        if (findViewById<EditText>(R.id.customerNameEditText).text.toString() == "")
            results.text = "No Customer Logged\n"
        if (drivingLog == "")
            results.text = results.text.toString() + "No Driving Logged\n"
        if (findViewById<TextView>(R.id.loggedMowEventsTextView).text == "")
            results.text = results.text.toString() + "No logged mowing\n"
        if (findViewById<TextView>(R.id.loggedWeedEatingEventsTextView).text == "")
            results.text = results.text.toString() + "No logged weeding\n"
        if (results.text != "")
            return false
        return true

    }

    private fun prepDataForSaving(customerName: String, drivingLog: String, mowingLog: String, weedingLog: String, jobNotes: String = "" ) : String {
        return customerName + "\n;" + drivingLog + ";" + mowingLog + ";" + weedingLog + "\n;" + jobNotes + "~\n"
    }

    fun writeToInternalFile(job : String) {
        var oldData : String = ""
        val fileName = "loggedmowing"
        if (File("/data/data/com.tutorialkart.jandjlawnserviceapp/files/", fileName).exists())
            oldData = readFromInternalFile()

        val outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
        val writer = PrintWriter(outputStream)

        // Write each task on a separate line
        val fileContents = job
        writer.println(oldData + fileContents)
        writer.close()
    }
    fun readFromInternalFile(): String {

        val fileName = "loggedmowing"

        val inputStream = openFileInput(fileName)
        val reader = inputStream.bufferedReader()
        val stringBuilder = StringBuilder()
        val lineSeparator = System.getProperty("line.separator")

        // Append each task to stringBuilder
        reader.forEachLine { stringBuilder.append(it).append(lineSeparator) }

        return stringBuilder.toString()
    }

    fun currentDateAndTimeDep()
    {
        val test = java.util.Calendar.getInstance()
        Toast.makeText(this, test.toString(), Toast.LENGTH_LONG).show()
    }
    @SuppressLint("NewApi")
    fun currentDateAndTime(format: String = "Date And Time"): String {
        var date = LocalDateTime.now().toString().split("T")
        var time = date[1].split(":")
        var yearmonthday = date[0]
        var hour = time[0]
        var minute = time[1]
        var second = time[2].split(".")

        if (format == "Time")
        {
            return hour + ":" + minute + ":" + second[0]
        }
        return yearmonthday + " --- " + hour + " : " + minute + " : " + second[0]
    }

    private fun trackLocation() {

        // Create location request
        locationRequest = LocationRequest.create()
            .setInterval(15000)
            .setFastestInterval(5000)
            //priority must be high accuracy
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Create location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    updateMap(location)
                }
            }
        }
        client = LocationServices.getFusedLocationProviderClient(this)
    }
    //update map, run schedule based on trackLocation.locationRequest.interval ^^^
    private fun updateMap(location: Location) {

        // Get current location
        val currentLatLng = LatLng(location.latitude,
            location.longitude)

        val geocoder = Geocoder(this)
        val addressList = geocoder.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1)
        locationInfo = addressList[0].getAddressLine(0)
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
    override fun onPause() {
        super.onPause()
        client?.removeLocationUpdates(locationCallback!!)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            client?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, Looper.getMainLooper())
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

                    val mowingTimesDisplay : TextView = findViewById(R.id.loggedMowEventsTextView)
                    val weedingTimesDisplay : TextView = findViewById(R.id.loggedWeedEatingEventsTextView)
                    //collect all data
                    val customerName = findViewById<EditText>(R.id.customerNameEditText).text.toString()
                    val jobNotes = findViewById<EditText>(R.id.jobNotes).text.toString()
                    val jobData = prepDataForSaving(
                        customerName,
                        drivingLog,
                        mowingTimesDisplay.text.toString(),
                        weedingTimesDisplay.text.toString(),
                        jobNotes
                    )
                    //log it to an internal file
                    writeToInternalFile(jobData)
                    //clear the fields
                    clearFields()
                    Toast.makeText(this, "Job successfully logged", Toast.LENGTH_LONG).show()
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

}