package com.example.happyplacesapp.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.happyplacesapp.MainActivity
import com.example.happyplacesapp.database.DatabaseHandler
import com.example.happyplacesapp.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplacesapp.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity() {
    private var binding: ActivityAddHappyPlaceBinding? = null


    /**
     * An variable to get an instance calendar using the default time zone and locale.
     */
    private var cal = Calendar.getInstance()

    /**
     * A variable for DatePickerDialog OnDateSetListener.
     * The listener used to indicate the user has finished selecting a date. Which we will be initialize later on.
     */
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var saveImageToInternalStorage: Uri? = null


    private var mLatitude: Double = 0.0 // A variable which will hold the latitude value.
    private var mLongitude: Double = 0.0 // A variable which will hold the longitude value.


    private var mHappyPlaceDetails: HappyPlaceModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.hide() //this is for hide action bar
        binding!!.btnBack.setOnClickListener {
            onBackPressed()
        }

        //Receive data when swipe to edit and send from intent
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel

        }

        //set all values for edit
        if (mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"

            //set all values from recyclerView and swipe to edit into edit text.
            binding!!.etTitle.setText(mHappyPlaceDetails!!.title)
            binding!!.etDescription.setText(mHappyPlaceDetails!!.description)
            binding!!.etDate.setText(mHappyPlaceDetails!!.date)
            binding!!.etLocation.setText(mHappyPlaceDetails!!.location)

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

            binding!!.ivPlaceImage.setImageURI(saveImageToInternalStorage)

            binding?.btnSave?.text = "UPDATE" //set Button text


        }

        // https://www.tutorialkart.com/kotlin-android/android-datepicker-kotlin-example/
        // create an OnDateSetListener
        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                updateDateInView()
            }
        //This is for when run the application and than automatic fill current date from the calendar into edit text
        updateDateInView() // Here the calender instance what we have created before will give us the current date which is formatted in the format in function


        //This is for date select button
        binding!!.etDate.setOnClickListener {
            DatePickerDialog(this,dateSetListener, // This is the variable which have created globally and initialized in setupUI method.
                // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR), // Here the cal instance is created globally and used everywhere in the class where it is required.
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        //This is for add Image Button
        binding!!.tvAddImage.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
            pictureDialog.setItems(pictureDialogItems){ dialog,which ->
                when(which){
                    // Here we have create the methods for image selection from GALLERY
                    0 -> choosePhotoFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
            //Dialog show
            pictureDialog.show()
        }
        //this is for save button
        binding!!.btnSave.setOnClickListener {
            when{
                binding!!.etTitle.text.isNullOrEmpty() ->{
                    Toast.makeText(this,"Please Enter title",Toast.LENGTH_SHORT).show()
                }
                binding!!.etDescription.text.isNullOrEmpty() ->{
                    Toast.makeText(this,"Please Enter description",Toast.LENGTH_SHORT).show()
                }
                binding!!.etLocation.text.isNullOrEmpty() ->{
                    Toast.makeText(this,"Please Enter location",Toast.LENGTH_SHORT).show()
                }
                saveImageToInternalStorage == null ->{
                    Toast.makeText(this,"Please add image",Toast.LENGTH_SHORT).show()
                }
                else ->{
                    // Assigning all the values to data model class.
                    val happyPlaceModel = HappyPlaceModel(
                        if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                        binding!!.etTitle.text.toString(),
                        saveImageToInternalStorage.toString(),
                        binding!!.etDescription.text.toString(),
                        binding!!.etDate.text.toString(),
                        binding!!.etLocation.text.toString(),
                        mLatitude,
                        mLongitude
                    )

                    // Here we initialize the database handler class.
                    val dbHandler = DatabaseHandler(this)

                    if (mHappyPlaceDetails == null){
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                        if (addHappyPlace > 0){
                            setResult(Activity.RESULT_OK)
                            finish() //finishing activity
                        }
                    }else{
                        //this is for update data
                        val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                        setResult(Activity.RESULT_OK)
                        finish() //finishing activity
                        Toast.makeText(this,"Update Successfully",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }


    /**
     * A function to update the selected date in the UI with selected format.
     * This function is created because every time we don't need to add format which we have added here to show it in the UI.
     */
    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault()) // A date format
        binding!!.etDate.setText(sdf.format(cal.time).toString()) // A selected date using format which we have used is set to the UI.
    }
    /**
     * A method is used for image selection from GALLERY / PHOTOS of phone storage.
     */
    private fun choosePhotoFromGallery() {
       Dexter.withContext(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
           android.Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object : MultiplePermissionsListener{

           override fun onPermissionsChecked(repoart: MultiplePermissionsReport?) {
               // Here after all the permission are granted launch the gallery to select and image.
               if (repoart!!.areAllPermissionsGranted()){
                   val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                   startActivityForResult(galleryIntent, GALLERY)
               }
           }

           override fun onPermissionRationaleShouldBeShown(
               permission: MutableList<PermissionRequest>?,
               token: PermissionToken?
           ) {
              //after permission is denied
               showRationalDialogForPermissions()
           }

           }).onSameThread().check()

       }
    /**
     * A method is used  asking the permission for camera and storage and image capturing and selection from Camera.
     */
    private fun takePhotoFromCamera() {
        Dexter.withContext(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA).withListener(object : MultiplePermissionsListener{

            override fun onPermissionsChecked(repoart: MultiplePermissionsReport?) {
                // Here after all the permission are granted launch the CAMERA to capture an image.
                if (repoart!!.areAllPermissionsGranted()){
                   //This is for Launch the camera
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permission: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                //after permission is denied
                showRationalDialogForPermissions()
            }

        }).onSameThread().check()

    }



    //this is for receive result
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null){
                    val contentURI = data.data
                    try {
                        // Here this is used to get an bitmap from URI
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        //for save image
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Save Image : ", "Path :: $saveImageToInternalStorage")

                        binding!!.ivPlaceImage.setImageBitmap(selectedImageBitmap)  // Set the selected image from GALLERY to imageView.
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this,"Failed!",Toast.LENGTH_SHORT).show()
                    }
                }
                //this is for take picture from camera and set image into imageView.
            } else if (requestCode == CAMERA) {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap// Bitmap from camera
                //this is for save
                saveImageToInternalStorage =
                    saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

                //This is for setImage from Camera into ImageView
                binding!!.ivPlaceImage.setImageBitmap(thumbnail) // Set to the imageView.

            }else if (resultCode == Activity.RESULT_CANCELED) {
                Log.e("Cancelled", "Cancelled")
            }


        }
    }



    /**
     * A function used to show the alert dialog when the permissions are denied and need to allow it from settings app info.
     */
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show() //dialog show
        }

    /**
     * A function to save a copy of an image to internal storage for HappyPlaceApp to use.
     */
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri{
        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)

        // Initializing a new file
        // The bellow line return a directory in internal storage
        /**
         * The Mode Private here is
         * File creation mode: the default mode, where the created file can only
         * be accessed by the calling application (or all applications sharing the
         * same user ID).
         */

        var file = wrapper.getDir(IMAGE_DIRECTORY ,Context.MODE_PRIVATE)

        // Create a file to save the image
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
        }
        // Return the saved image uri
        return Uri.parse(file.absolutePath)

    }

    //this created companion object
    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlaceImages"
    }

}


