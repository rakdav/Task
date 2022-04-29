package com.bignerdranch.android.criminalintent

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
import java.io.File
import java.nio.channels.InterruptibleChannel
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_FOTO = 2
private const val DATE_FORMAT="EEE, MMM, dd"

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks {

    private lateinit var task: Task
    private lateinit var photoFile:File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton:Button
    private lateinit var suspectButton:Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView:ImageView

    private val taskDetailViewModel: TaskDetailViewModel by lazy {
        ViewModelProviders.of(this).get(TaskDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = Task()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        taskDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton=view.findViewById(R.id.task_report) as Button
        suspectButton=view.findViewById(R.id.task_suspect) as Button
        photoButton=view.findViewById(R.id.camera)
        photoView=view.findViewById(R.id.task_foto)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        taskDetailViewModel.loadCrime(crimeId)
        taskDetailViewModel.taskLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.task = crime
                    photoFile=taskDetailViewModel.getPhoto(crime)
                    photoUri=FileProvider.getUriForFile(requireActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",photoFile)
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                task.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                task.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(task.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }
        reportButton.setOnClickListener{
            Intent(Intent.ACTION_SEND).apply {
                type="text/plain"
                putExtra(Intent.EXTRA_TEXT,getTaskReport())
                putExtra(Intent.EXTRA_SUBJECT,getString(R.string.task_report_subject))
            }.also { intent -> //startActivity(intent)
            val chooserIntent=Intent.createChooser(intent,getString(R.string.send_report))
            startActivity(chooserIntent)}
        }
        suspectButton.apply {
            val pickContactIntent=Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener{
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
            val packageManager:PackageManager=requireActivity().packageManager
            val resolveActivity:ResolveInfo=packageManager.resolveActivity(pickContactIntent,
            PackageManager.MATCH_DEFAULT_ONLY)
            if(resolveActivity==null) isEnabled=false
        }
        photoButton.apply {
            val packageManager:PackageManager=requireActivity().packageManager
            val captureImage=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolveActivity:ResolveInfo?=packageManager.resolveActivity(captureImage,PackageManager.MATCH_DEFAULT_ONLY)
            if(resolveActivity==null) isEnabled=false
            setOnClickListener{
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
                val cameraActivities:List<ResolveInfo> = packageManager.
                      queryIntentActivities(captureImage,PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities)
                {
                    requireActivity().grantUriPermission(cameraActivity.activityInfo.packageName,
                    photoUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImage, REQUEST_FOTO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        taskDetailViewModel.saveCrime(task)
    }

    override fun onDateSelected(date: Date) {
        task.date = date
        updateUI()
    }

    private fun updateUI() {
        titleField.setText(task.title)
        dateButton.text = task.date.toString()
        solvedCheckBox. apply {
            isChecked = task.isSolved
            jumpDrawablesToCurrentState()
        }
        if(task.suspect.isNotEmpty())
        {
            suspectButton.text=task.suspect
        }
    }
    private fun updatePhotoView()
    {
        if(photoFile.exists()){
           //val bitmap=
        }
    }
    private fun getTaskReport():String
    {
        val solvedString=if(task.isSolved){
            getString(R.string.task_report_solved)
        }
        else
        {
            getString(R.string.task_report_unsolved)
        }
        val dateString=DateFormat.format(DATE_FORMAT,task.date).toString()
        val suspect=if(task.suspect.isBlank()){
            getString(R.string.task_report_no_suspect)
        }
        else
        {
            getString(R.string.task_report_suspect,task.suspect)
        }
        return getString(R.string.task_report,task.title,dateString,solvedString,suspect)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when{
            resultCode!=Activity.RESULT_OK->
                return
            requestCode== REQUEST_CONTACT&&data!=null->{
                val contactUri:Uri?=data.data
                val queryFields= arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor=requireActivity().contentResolver.query(contactUri,queryFields,null,null,null)
                cursor?.use {
                    if (it.count==0) return
                    it.moveToFirst()
                    val suspect=it.getString(0)
                    task.suspect=suspect
                    taskDetailViewModel.saveCrime(task)
                    suspectButton.text=suspect
                }
            }
        }
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
}