package eber.loreto.criminallntent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import eber.loreto.criminallntent.databinding.FragmentCrimeDetailBinding
import java.util.zip.Inflater

private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeDetailFragment : Fragment() {

    //private lateinit var binding: FragmentCrimeDetailBinding
    private var _binding: FramentCrimeDetailBinding? = null
    private val binding
            get() = checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible?"
            }


    private val ards: CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
   // Handle the result
        uri?.let { parseContactSelection(it) }
    }
    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
    // Handle the result
        if (didTakePhoto && photoName != null) {
            crimeDetailViewModel.updateCrime { oldCrime ->
                oldCrime.copy(photoFileName = photoName)
            }
        }
        private var photoName: String? = null
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): view? {
        _binding =
            FragmentCrimeDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
      }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                //crime = crime.copy(title = text.toString())
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            //crimeDate.apply {
                //text = crime.date.toString()
                //isEnabled = false
            //}

            crimeSolved.setOnCheckedChangeListener{ _, ischecked ->
                //crime = crime.copy(isSolved = ischecked)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }

            crimeSuspect.setOnClickListener {
                selectSuspect.launch(null)
            }

            val selectSuspectIntent = selectSuspect.contract.createIntent(
                requireContext(),
                null
            )
            crimeSuspect.isEnabled = canResolveIntent(selectSuspectIntent)

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    crimeDetailViewModel.crime.collect { crime ->
                        crime?.let { updateUi(it) }
                    }
                }
            }
            crimeCamera.setOnClickListener {
                    photoName = "IMG_${Date()}.JPG"
                val photoFile = File(requireContext().applicationContext.filesDir,
                    photoName)
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    photoFile
                )
                takePhoto.launch(photoUri)
            }
            val captureImageIntent = takePhoto.contract.createIntent(
                requireContext(),
                null
            )
            crimeCamera.isEnabled = canResolveIntent(captureImageIntent)

        }

        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
        //
            val newDate =
                bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            crimeDetailViewModel.updateCrime { it.copy(date = newDate) }

        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        private fun updateUi(crime: Crime) {
            binding.apply {
                if (crimeTitle.text.toString() != crime.title) {
                    crimeTitle.setText(crime.title)
                }
                crimeDate.text = crime.date.toString()
                crimeDate.setOnClickListener {
                    findNavController().navigate(
                        CrimeDetailFragmentDirections.selectDate(crime.date)
                    )
                }
                crimeSolved.isChecked = crime.isSolved

                crimeReport.setOnClickListener {
                    val reportIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            getString(R.string.crime_report_subject)
                        )

                        crimeSuspect.text = crime.suspect.ifEmpty {
                            getString(R.string.crime_suspect_text)
                        }
                    }
                    //startActivity(reportIntent)
                    val chooserIntent = Intent.createChooser(
                        reportIntent,
                        getString(R.string.send_report)
                    )
                    startActivity(chooserIntent)
                }
                updatePhoto(crime.photoFileName)

            }
        }

        private fun getCrimeReport(crime: Crime): String {
            val solvedString = if (crime.isSolved) {
                getString(R.string.crime_report_solved)
            } else {
                getString(R.string.crime_report_unsolved)
            }
            val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
            val suspectText = if (crime.suspect.isBlank()) {
                getString(R.string.crime_report_no_suspect)
            } else {
                getString(R.string.crime_report_suspect, crime.suspect)
            }
            return getString(
                R.string.crime_report,
                crime.title, dateString, solvedString, suspectText
            )
        }

        private fun parseContactSelection(contactUri: Uri) {
            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
            val queryCursor = requireActivity().contentResolver
                .query(contactUri, queryFields, null, null, null)
            queryCursor?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val suspect = cursor.getString(0)
                    crimeDetailViewModel.updateCrime { oldCrime ->
                        oldCrime.copy(suspect = suspect)
                    }
                }
            }
        }

        private fun canResolveIntent(intent: Intent): Boolean {
            //intent.addCategory(Intent.CATEGORY_HOME)
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            return resolvedActivity != null
        }

        private fun updatePhoto(photoFileName: String?) {
            if (binding.crimePhoto.tag != photoFileName) {
                val photoFile = photoFileName?.let {
                    File(requireContext().applicationContext.filesDir, it)
                }
                if (photoFile?.exists() == true) {
                    binding.crimePhoto.doOnLayout { measuredView ->
                        val scaledBitmap = getScaledBitmap(
                            photoFile.path,
                            measuredView.width,
                            measuredView.height
                        )
                        binding.crimePhoto.setImageBitmap(scaledBitmap)
                        binding.crimePhoto.tag = photoFileName
                        binding.crimePhoto.contentDescription =
                            getString(R.string.crime_photo_image_description)

                    }
                } else {
                    binding.crimePhoto.setImageBitmap(null)
                    binding.crimePhoto.tag = null
                    binding.crimePhoto.contentDescription =
                        getString(R.string.crime_photo_no_image_description)
                }
            }
        }
    }
}
