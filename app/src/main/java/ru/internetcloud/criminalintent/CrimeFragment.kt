package ru.internetcloud.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ru.internetcloud.criminalintent.dialog.DatePickerFragment
import ru.internetcloud.criminalintent.dialog.IncreasedImageFragment
import ru.internetcloud.criminalintent.dialog.TimePickerFragment
import ru.internetcloud.criminalintent.model.Crime
import ru.internetcloud.criminalintent.model.CrimeDetailViewModel
import ru.internetcloud.criminalintent.utils.DateFormatter
import ru.internetcloud.criminalintent.utils.getScaledBitmap
import java.io.File
import java.util.*

private const val TAG = "rustam"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 2
private const val REQUEST_PHOTO = 3
private const val DIALOG_INCREASED_IMAGE = "IncreasedImage"

private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        //Log.d(TAG, "args bundle crime ID: $crimeId")

        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(), "ru.internetcloud.criminalintent.fileprovider", photoFile)
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        // TextWatcher нужно навешивать здесь, а не в onCreate или onCreateView, т.к. там еще не восстановлено
        // EditText и слушатели будут "дергаться" лишний раз

        // Log.i(TAG, "Процедура onStart, содержимое EditText уже восстановлено к этому моменту после поворота," +
        //            " оно заполнено: titleField.text = " + titleField.text)

        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            // Это пространство оставлено пустым специально
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
            // И это
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked -> crime.isSolved = isChecked }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_suspect))
            }.also {
                intent -> startActivity(intent)
            }
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            // псевдо-код для проверки отсутствия книги контактов: pickContactIntent.addCategory(Intent.CATEGORY_HOME)
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false // блокируем кнопку если на устройстве нет книги контактов
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(cameraActivity.activityInfo.packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        photoView.apply {
            setOnClickListener {

                // показать увеличенную фотографию:
                if (photoFile != null && photoFile.exists()) {
                    val fragmentManager = requireActivity().supportFragmentManager
                    val increasedImageFragment: IncreasedImageFragment = IncreasedImageFragment.newInstance(photoFile)
                    increasedImageFragment.show(fragmentManager, DIALOG_INCREASED_IMAGE)
                }

            }
        }
    }

    override fun onStop() {
        super.onStop()

        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data

                // Указать, для каких полей ваш запрос должен возвращать значения.
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

                // Выполняемый здесь запрос — contactUri похож на предложение "where"

                if (contactUri != null) {
                    val cursor = requireActivity().contentResolver
                        .query(contactUri, queryFields, null, null, null)
                    cursor?.use {
                        // Verify cursor contains at least one result
                        if (it.count == 0) {
                            return
                        }

                        // Первый столбец первой строки данных —
                        // это имя вашего подозреваемого.
                        it.moveToFirst()
                        val suspect = it.getString(0)
                        crime.suspect = suspect
                        crimeDetailViewModel.saveCrime(crime)
                        suspectButton.text = suspect
                    }
                }
            }
            requestCode == REQUEST_PHOTO ->  {
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = DateFormatter.getDateString(crime.date)
        timeButton.text = DateFormatter.getTimeString(crime.date)
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState() // чтобы не было анимации при отрисовке флажка
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
        updatePhotoView()
    }

    override fun onDateSelected(date: Date) {

        // часы и минуты оставляю неизменными, меняю только Год, Месяц, День.

        val oldCalendar = Calendar.getInstance()
        oldCalendar.time = crime.date //устанавливаем дату, с которой будет производить операции

        val hour = oldCalendar[Calendar.HOUR_OF_DAY]
        val minute = oldCalendar[Calendar.MINUTE]

        val newCalendar = Calendar.getInstance()
        newCalendar.time = date //устанавливаем дату, с которой будет производить операции

        newCalendar.add(Calendar.HOUR_OF_DAY, hour)
        newCalendar.add(Calendar.MINUTE, minute)
        val newDate = newCalendar.time // получаем измененную дату

        crime.date = newDate
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report,
            crime.title, dateString, solvedString, suspect)
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

}