package ru.hawoline.textrecognizerline

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.ctx
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image_url_field.setOnEditorActionListener{ _, action, _ ->
            if(action == EditorInfo.IME_ACTION_DONE){

                try {
                    Picasso.with(ctx).load(image_url_field.text.toString()).into(image_holder)
                } catch (e: Exception){
                    Toast.makeText(this, R.string.image_not_loaded, Toast.LENGTH_LONG).show()
                }

                true
            }

            false
        }

        choose_image_from_gallery_btn.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    pickImageFromGallery();
                }
            }
            else{
                pickImageFromGallery();
            }
        }
    }

    fun recognizeText(view: View) {
        if (image_holder.drawable == null){
            Toast.makeText(this, R.string.image_null, Toast.LENGTH_LONG).show()
            return
        }
        val textImage = FirebaseVisionImage.fromBitmap(
            (image_holder.drawable as BitmapDrawable).bitmap
        )

        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

        var detectedText = ""
        detector.processImage(textImage).addOnSuccessListener { firebaseVisionText ->
            for (block in firebaseVisionText.textBlocks) {

                for (line in block.lines) {
                    val lineText = line.text

                    detectedText += lineText + "\n"
                }
            }

            detected_text_et.setText(detectedText)
        }



        detector.close()
    }
    fun copyText(view: View) {
        val clipboardService = getSystemService(Context.CLIPBOARD_SERVICE)
        val clipboardManager: ClipboardManager = clipboardService as ClipboardManager
        val srcText: String = detected_text_et.getText().toString()

        val clipData = ClipData.newPlainText("Source Text", srcText)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000;
        private const val PERMISSION_CODE = 1001;
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            image_holder.setImageURI(data?.data)
        }
    }
}
