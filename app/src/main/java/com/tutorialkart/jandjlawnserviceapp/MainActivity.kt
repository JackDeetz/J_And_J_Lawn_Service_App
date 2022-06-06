package com.tutorialkart.jandjlawnserviceapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime

class MainActivity : AppCompatActivity(), BackupDataDialog.DialogFragmentListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener{
            startActivity(Intent(this, StartAJobActivity::class.java))}

        findViewById<Button>(R.id.backupDataButton).setOnClickListener{
            val dialog = BackupDataDialog()
            dialog.show(supportFragmentManager, "Backup Dialog")
        }

        findViewById<Button>(R.id.addClientButton).setOnClickListener{
            startActivity(Intent(this, AddAClient::class.java))}

        findViewById<Button>(R.id.scheduleJobButton).setOnClickListener{
            startActivity(Intent(this, ScheduleJob::class.java))
        }

    }

    fun implicitIntentChooserButtonClicked(fileName : String) {
        val intent = Intent(Intent.ACTION_SEND)

// Supply extra that is plain text
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "work Data")
        intent.putExtra(Intent.EXTRA_TEXT, readFromInternalFile(fileName))

// If at least one app can handle intent, allow user to choose
        if (intent.resolveActivity(packageManager) != null) {
            val chooser = Intent.createChooser(intent, "Share URL")
            startActivity(chooser)
        }

    }
    fun readFromInternalFile(fileName : String): String {

        val inputStream = openFileInput(fileName)
        val reader = inputStream.bufferedReader()
        val stringBuilder = StringBuilder()
        val lineSeparator = System.getProperty("line.separator")

        // Append each task to stringBuilder
        reader.forEachLine { stringBuilder.append(it).append(lineSeparator) }

        return stringBuilder.toString()
    }

    override fun jobsButtonPressed(dialog: DialogFragment) {
        val fileName = "loggedmowing"
        if (File("/data/data/com.tutorialkart.jandjlawnserviceapp/files/", fileName).exists())
            implicitIntentChooserButtonClicked(fileName)
    }

    override fun clientsButtonPressed(dialog: DialogFragment) {
        val fileName = "loggedClients"
        if (File("/data/data/com.tutorialkart.jandjlawnserviceapp/files/", fileName).exists())
            implicitIntentChooserButtonClicked(fileName)
    }

}