package com.example.pocimagetopdf

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupListeners()
    }

    private fun setupListeners() {
        btnPickImage.setOnClickListener {
            pickImage(it)
        }
    }

    private fun pickImage(v: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 120)
        } else {
            Toast.makeText(this, "bla", Toast.LENGTH_LONG).show()
            requestPermission()
       }
    }

    private fun requestPermission() {
        TODO("Not yet implemented")
        ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 120 && resultCode == RESULT_OK && data != null) {

            val selectedImageUri = data.data

            val filePath = (MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(
                selectedImageUri!!,
                arrayOf(filePath), null, null, null
            )
            cursor?.moveToFirst()

            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(filePath)
                val myPath = cursor.getString(columnIndex)

                cursor.close()

//                var bitmap = BitmapFactory.decodeFile(myPath)
                var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                imageView.setImageBitmap(bitmap)

                val pdfDocument = PdfDocument()
                val pi = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = pdfDocument.startPage(pi)
                val canvas = page.canvas
                val paint = Paint()
                paint.color = ContextCompat.getColor(this, R.color.colorAccent)
                canvas.drawPaint(paint)
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
                paint.color = Color.BLUE
                canvas.drawBitmap(bitmap, Rect(), Rect(), null)
                pdfDocument.finishPage(page)

                val root = File(Environment.getRootDirectory(), "PDF Folder 12")
                if (!root.exists()) {
                    root.mkdir()
                }
                val file = File(root, "picture.pdf")
                try {
                    val fileOutPutStream = FileOutputStream(file)
                    pdfDocument.writeTo(fileOutPutStream)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                pdfDocument.close()
            }
        }
    }
}