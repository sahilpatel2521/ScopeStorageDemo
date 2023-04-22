package com.example.selectfile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    lateinit var selectFile : Button
    lateinit var createFile : Button
    lateinit var downloadImageToDownloadFolder : Button
    lateinit var openFolder : Button
    lateinit var fetchImageLocation : Button

    lateinit var downloadImageToAppFolder : Button

    lateinit var image : ImageView

    companion object{
        private const val OPEN_FILE_REQUEST_CODE = 1
        private const val OPEN_FOLDER_REQUEST_CODE = 2
        private const val MEDIA_LOCATION_PERMISSION_REQUEST_CODE = 3
        private const val CHOOSE_FILE = 4

        var downloadImageUrl = "https://cdn.pixabay.com/photo/2020/04/21/06/41/bulldog-5071407_1280.jpg"
        private const val PERMISSION_READ_EXTERNAL_STORAGE = 5

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectFile = findViewById<Button>(R.id.open_file)
        createFile = findViewById<Button>(R.id.create_file)
        downloadImageToDownloadFolder = findViewById<Button>(R.id.download_image)
        openFolder = findViewById<Button>(R.id.open_folder)
        fetchImageLocation = findViewById<Button>(R.id.download_image_media_location)
        downloadImageToAppFolder = findViewById<Button>(R.id.download_image_internal)
        image = findViewById<ImageView>(R.id.image)

        selectFile.setOnClickListener{
            openFile()
        }

        createFile.setOnClickListener{
            startActivity(Intent(this@MainActivity,FileActivity::class.java))
        }

        downloadImageToDownloadFolder.setOnClickListener {
            downloadImage()
        }

        openFolder.setOnClickListener {
                openFolderLocation()
        }

        fetchImageLocation.setOnClickListener {
                fetchMediaLocation()
        }

        downloadImageToAppFolder.setOnClickListener{
            downloadImageToAppFolderFile()

        }
    }

    private fun downloadImageToAppFolderFile() {
        if (Build.VERSION.SDK_INT<= Build.VERSION_CODES.Q){
            if (checkSelfPermissionReadWrite(this)){
                downloadToInternalFolder()
            }else{
                requestPermissionForReadWrite(this)
            }
        }else{
            downloadToInternalFolder()
        }
    }

    private fun downloadToInternalFolder() {
        try {
            val file = File(
                    this.getExternalFilesDir(
                    null
                ),"SampleImageDemo.png"
            )
            if (!file.exists())
                file.createNewFile()

            var fileOutputStream : FileOutputStream? = null

            fileOutputStream = FileOutputStream(file)
            val bitmap = (image.drawable as BitmapDrawable).bitmap


            bitmap?.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream)


            Toast.makeText(applicationContext,"Download Successfully" +file.absolutePath
                ,Toast.LENGTH_LONG).show()
        }catch (e : Exception){

        }
    }

    /*   @SuppressLint("NewApi")
       private fun downloadPdfFile() {
           // create a new document
           val document = PdfDocument()
           // crate a page description
           val pageInfo = PdfDocument.PageInfo.Builder(400, 300, 1).create()
           // start a page
           val page = document.startPage(pageInfo)
           val canvas = page.canvas
           val paint = Paint()
           canvas.drawText("HelloWorld", 80F, 50F, paint)
           // finish the page
           document.finishPage(page)

           //Make IS_PENDING 1 so that it is not visible to other apps till the time this is downloaded
           val values = ContentValues().apply {
               put(MediaStore.Downloads.DISPLAY_NAME, "demofile_" + Random.nextInt(9999) + ".pdf")
               put(MediaStore.Downloads.IS_PENDING, 1)
           }

           val resolver = contentResolver

           //Storing at primary location
           val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

           //Insert the item
           val item = resolver.insert(collection, values)


           if (item != null) {
               resolver.openOutputStream(item).use { out ->
                   document.writeTo(out);
               }
           }
           values.clear()

           //Make it 0 when downloaded
           values.put(MediaStore.Images.Media.IS_PENDING, 0)
           item?.let { resolver.update(it, values, null, null) }

           Toast.makeText(
               applicationContext,
               "Download successfully to ${item?.path}",
               Toast.LENGTH_LONG
           ).show()


       }*/

    private fun fetchMediaLocation() {
        if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.Q){
             openChooser()
        }else{
            if (isPermissionGrantedForMediaLocationAccess(this)){
                openChooser()
            }else{
                requestPermissionForMediaLocation(this)
            }
        }
    }

    private fun openChooser() {
        val intent  = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false)
        intent.action =Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), CHOOSE_FILE)
    }

    private fun openFolderLocation() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_FOLDER_REQUEST_CODE)
    }

    private fun downloadImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                if (checkSelfPermissionReadWrite(this)){
                    downloadImageIntoDownloadFolder()
                }else{
                    requestPermissionForReadWrite(this)
                }
        }else{
            downloadImageIntoDownloadFolder()
        }
    }

    private fun openFile() {
        val intent =Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            if (requestCode == OPEN_FILE_REQUEST_CODE){
                data?.data?.also { documentUri ->
                    contentResolver.takePersistableUriPermission(
                        documentUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Toast.makeText(this,documentUri.path.toString(),Toast.LENGTH_LONG).show()
                }
            }
            else if (requestCode == OPEN_FOLDER_REQUEST_CODE){
                val directoryUri = data?.data?: return
                    //taking permission
                contentResolver.takePersistableUriPermission(
                    directoryUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                // now you access the folder
                val documentTree = DocumentFile.fromTreeUri(this,directoryUri)?: return
                val childDocuments = documentTree.listFiles().asList()
                Toast.makeText(
                    this,
                    "Total Items Under this folder =" + childDocuments.size.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
            else if(requestCode == CHOOSE_FILE){
                if (data != null){
                    var inputStream : InputStream? = null
                    try {
                        inputStream = contentResolver.openInputStream(data.data!!)
                        val exifInterface = ExifInterface(inputStream!!)

                        Toast.makeText(
                            this,
                            "Path = " + data.data + "   ,Latitude = " + exifInterface.getAttribute(
                                ExifInterface.TAG_GPS_LATITUDE
                            ) + "   ,Longitude =" + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
                            Toast.LENGTH_LONG
                        ).show()
                    }catch (e : IOException){

                    }
                }
            }
        }
    }

    //Request Permission For Read Storage
    private fun isPermissionGrantedForMediaLocationAccess(context: Context): Boolean {
        val result: Int =
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionForReadWrite(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ), PERMISSION_READ_EXTERNAL_STORAGE
        )
    }

    private fun checkSelfPermissionReadWrite(context: Context): Boolean{
        val result : Int = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun downloadImageIntoDownloadFolder(){
        val mgr = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(downloadImageUrl)
        val request = DownloadManager.Request(downloadUri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false).setTitle("Sample")
            .setDescription("Sample Image Demo New")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "SampleImage.jpg"
            )
        Toast.makeText(
            this,
            "Download successfully to ${downloadUri?.path}",
            Toast.LENGTH_LONG
        ).show()
        mgr.enqueue(request)

    }

    fun requestPermissionForMediaLocation(context: Context){
        ActivityCompat.requestPermissions(
            context as Activity
            , arrayOf(android.Manifest.permission.ACCESS_MEDIA_LOCATION)
            , MEDIA_LOCATION_PERMISSION_REQUEST_CODE
        )
    }

}