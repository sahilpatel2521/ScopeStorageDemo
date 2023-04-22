package com.example.selectfile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FileActivity : AppCompatActivity() {
    companion object {
        private const val CREATE_FILE_REQUEST_CODE = 1
    }

    lateinit var fileName : EditText
    lateinit var fileContent : EditText
    lateinit var createFile : Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        fileName = findViewById<EditText>(R.id.file_name)
        fileContent = findViewById<EditText>(R.id.file_content)
        createFile = findViewById<Button>(R.id.file_create)

        createFile.setOnClickListener(){
            fileCreate()
        }

    }

    private fun fileCreate() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, "${fileName.text}.txt")
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                writeFileContent(data.data)
            }
        }
    }

    private fun writeFileContent(data: Uri?) {
        try {
            val file = data?.let { this.contentResolver.openFileDescriptor(it,"w") }
            file?.let {
                val fileOutputStream = FileOutputStream(it.fileDescriptor)
                val textContent = fileContent.text.toString()
                fileOutputStream.write(textContent.toByteArray())
                fileOutputStream.close()
                it.close()
            }
        }catch (e : FileNotFoundException){

        }catch (e : IOException){

        }
    }
}