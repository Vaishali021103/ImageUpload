package com.example.firebasephotoupload

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    private lateinit var mButtonChooseImage: Button
    private lateinit var mButtonUpload: Button
    private lateinit var mTextViewShowUploads: TextView
    private lateinit var mEditTextFileName: EditText
    private lateinit var mImageView: ImageView
    private lateinit var mProgressBar: ProgressBar

    private var mImageUri: Uri? = null

    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference

    private var mUploadTask: StorageTask<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mButtonChooseImage = findViewById(R.id.button_choose_image)
        mButtonUpload = findViewById(R.id.button_upload)
        mTextViewShowUploads = findViewById(R.id.text_view_show_uploads)
        mEditTextFileName = findViewById(R.id.edit_text_file_name)
        mImageView = findViewById(R.id.image_view)
        mProgressBar = findViewById(R.id.progress_bar)

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")

        mButtonChooseImage.setOnClickListener {
            openFileChooser()
        }

        mButtonUpload.setOnClickListener {
            if (mUploadTask != null && mUploadTask!!.isInProgress) {
                Toast.makeText(this@MainActivity, "Upload in progress", Toast.LENGTH_SHORT).show()
            } else {
                uploadFile()
            }
        }

        mTextViewShowUploads.setOnClickListener {
            // Handle showing uploads
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.data != null) {
            mImageUri = data.data

            Picasso.with(this).load(mImageUri).into(mImageView)
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadFile() {
        mImageUri?.let { imageUri ->
            val fileReference: StorageReference = mStorageRef.child(System.currentTimeMillis().toString()
                    + "." + getFileExtension(imageUri))

            mUploadTask = fileReference.putFile(imageUri)
                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    val handler = Handler()
                    handler.postDelayed({
                        mProgressBar.progress = 0
                    }, 500)

                    Toast.makeText(this@MainActivity, "Upload successful", Toast.LENGTH_LONG).show()
                    val upload = Upload(mEditTextFileName.text.toString().trim(),
                        taskSnapshot.uploadSessionUri.toString())
                    val uploadId = mDatabaseRef.push().key
                    uploadId?.let {
                        mDatabaseRef.child(it).setValue(upload)
                    }
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                })
                .addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    mProgressBar.progress = progress.toInt()
                })
        } ?: run {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }
}
