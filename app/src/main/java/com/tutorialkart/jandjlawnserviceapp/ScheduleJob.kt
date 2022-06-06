package com.tutorialkart.jandjlawnserviceapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.material.datepicker.MaterialCalendar.newInstance
import java.io.File
import java.io.PrintWriter
import java.time.LocalDateTime

class ScheduleJob : AppCompatActivity() {
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_job)

        val datePicker = findViewById<TimePicker>(R.id.timePicker)
        val today = Calendar.getInstance()

        findViewById<TextView>(R.id.dateDisplayTextView).setOnClickListener{
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener
            {
                    view, myear, mmonth, mdayOfMonth -> findViewById<TextView>(R.id.dateDisplayTextView).text = (""+ mdayOfMonth +"/"+ mmonth +"/"+ myear)
            }, year, month, day)
                datePickerDialog.show()
            }
        }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.submit_clear_appbarr_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("NewApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Determine which menu option was selected
        return when (item.itemId) {
            R.id.app_bar_action_one_submit -> {
                if (everythingIsInOrderForSubmitingJob()) {

                    //collect all data
                    val clientName = findViewById<EditText>(R.id.clientNameEditView).text.toString()
                    val address = findViewById<EditText>(R.id.addressEditText).text.toString()
                    val date = findViewById<TextView>(R.id.dateDisplayTextView).text.toString()
                    val time = findViewById<TimePicker>(R.id.timePicker).hour.toString() + ":" +
                                findViewById<TimePicker>(R.id.timePicker).minute.toString()
                    val jobData = prepDataForSaving(
                        clientName,
                        address,
                        date,
                        time
                    )
                    //log it to an internal file
                    writeToInternalFile(jobData)
                    //clear the fields
                    clearFields()
                    Toast.makeText(this, "Job successfully Scheduled", Toast.LENGTH_LONG).show()
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

    private fun prepDataForSaving(clientName: String, address: String, date: String, time: String): String {
        return clientName + ";" + address + ";" + date + ";" + time + "~"

    }
    fun writeToInternalFile(client : String) {
        var oldData : String = ""
        val fileName = "schedJobs"
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

        val fileName = "schedJobs"

        val inputStream = openFileInput(fileName)
        val reader = inputStream.bufferedReader()
        val stringBuilder = StringBuilder()
        val lineSeparator = System.getProperty("line.separator")

        // Append each task to stringBuilder
        reader.forEachLine { stringBuilder.append(it).append(lineSeparator) }

        return stringBuilder.toString()
    }
    private fun everythingIsInOrderForSubmitingJob(): Boolean {
        val results : TextView = findViewById(R.id.submitResultsTextView)
        results.text = ""
        if (findViewById<EditText>(R.id.clientNameEditView).text.toString() == "")
            results.text = "No Client Entered"
        if (findViewById<EditText>(R.id.addressEditText).text.toString() == "")
            results.text = results.text.toString() +"No Client Entered"
        if (findViewById<TextView>(R.id.dateDisplayTextView).text == "")
            results.text = results.text.toString() +"No Date Entered"
        if (results.text.toString() != "")
            return false
        return true
    }

    private fun clearFields() {
        findViewById<EditText>(R.id.clientNameEditView).setText("")
        findViewById<EditText>(R.id.addressEditText).setText("")
        findViewById<TextView>(R.id.dateDisplayTextView).text = ""
    }
}