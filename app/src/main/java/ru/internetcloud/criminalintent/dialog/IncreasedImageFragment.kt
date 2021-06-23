package ru.internetcloud.criminalintent.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import ru.internetcloud.criminalintent.utils.getScaledBitmap
import java.io.File
import ru.internetcloud.criminalintent.R

private val ARG_FILE = "file"

class IncreasedImageFragment: DialogFragment() {

    private val crimePhotoFile: File? = null

    companion object {
        fun newInstance(file: File?): IncreasedImageFragment {
            val bundle = Bundle()
            bundle.putSerializable(ARG_FILE, file)
            return IncreasedImageFragment().apply { arguments = bundle }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val crimePhotoFile = arguments?.getSerializable(ARG_FILE) as File?

        val view: View = LayoutInflater.from(activity).inflate(resources.getLayout(R.layout.dialog_image), null)
        val increased_image_view = view.findViewById<ImageView>(R.id.increased_image_view)

        if (crimePhotoFile == null || !crimePhotoFile.exists()) {
            val ic_photo_camera = resources.getDrawable(R.drawable.ic_photo_camera_24)
            increased_image_view.setImageDrawable(ic_photo_camera)
        } else {
            val bitmap: Bitmap = getScaledBitmap(crimePhotoFile.getPath(), requireActivity())
            increased_image_view.setImageBitmap(bitmap)
        }

        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(view)
        alertDialogBuilder.setPositiveButton(android.R.string.ok, null)

        return alertDialogBuilder.create()
    }

}