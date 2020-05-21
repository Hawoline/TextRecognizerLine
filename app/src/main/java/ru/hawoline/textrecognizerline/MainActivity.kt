package ru.hawoline.textrecognizerline

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import kotlinx.android.synthetic.main.activity_main.*
import ru.hawoline.textrecognizerline.util.TextProcessor
import ru.hawoline.textrecognizerline.util.Updater
import java.net.InetAddress

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Updater.getInstance().createUpdateManager(applicationContext)
        Updater.getInstance().checkForUpdates(this)

        choose_image_from_gallery_btn.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else {
                    pickImageFromGallery()
                }
            }
        }

        detected_text_view.setOnClickListener {
            copyTextToClipBoard()
            Toast.makeText(this, getString(R.string.text_copy_success), Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            image_holder.setImageURI(data?.data)
        }
        if (requestCode == Updater.UPDATE_REQUEST) {
            if (resultCode != RESULT_OK) {
                Log.d("LOG", "Update not installed")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun recognizeText(view: View) {
        if (image_holder.drawable == null){
            Toast.makeText(this, R.string.image_null, Toast.LENGTH_LONG).show()
            return
        }

        recognizeTextFromCloud()
    }

    private fun recognizeTextFromCloud() {
        val detector = FirebaseVision.getInstance().cloudTextRecognizer
        val textImage = FirebaseVisionImage.fromBitmap((image_holder.drawable as BitmapDrawable).bitmap)

        detector.processImage(textImage)
            .addOnSuccessListener { firebaseVisionText ->
                detected_text_view.text = TextProcessor.process(firebaseVisionText)
            }
            .addOnFailureListener{ e ->
                Toast.makeText(this, R.string.text_recognition_failed, Toast.LENGTH_SHORT).show()
            }

        detector.close()
    }

    fun copyText(view: View) {
        copyTextToClipBoard()
        Toast.makeText(this, getString(R.string.text_copy_success), Toast.LENGTH_SHORT).show()
    }

    private fun copyTextToClipBoard() {
        val clipboardService = getSystemService(Context.CLIPBOARD_SERVICE)
        val clipboardManager: ClipboardManager = clipboardService as ClipboardManager
        val srcText: String = detected_text_view.text.toString()

        val clipData = ClipData.newPlainText("Source Text", srcText)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }
}
