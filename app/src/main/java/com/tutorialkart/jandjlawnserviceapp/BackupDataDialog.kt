package com.tutorialkart.jandjlawnserviceapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class BackupDataDialog : DialogFragment() {
    interface DialogFragmentListener {
        fun jobsButtonPressed(dialog : DialogFragment)
        fun clientsButtonPressed(dialog : DialogFragment)
    }

    lateinit var listener : DialogFragmentListener

    override fun onCreateDialog(savedInstanceState: Bundle?)
            : Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Backup Options")
        builder.setMessage("Choose Which Data To Backup:")
        builder.setPositiveButton("Jobs", DialogInterface.OnClickListener { dialog, id ->
            listener.jobsButtonPressed(this) })

        builder.setNegativeButton("Clients", DialogInterface.OnClickListener { dialog, id ->
            listener.clientsButtonPressed(this) })

        return builder.create()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as DialogFragmentListener
    }
}