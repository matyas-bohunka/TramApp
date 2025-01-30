package com.example.tramapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.tramapp.databinding.ActivityNewTramTypeBinding

class NewTramTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewTramTypeBinding
    private lateinit var database: AppDatabase
    private lateinit var tramTypeDao: TramTypeDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewTramTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this, lifecycleScope)
        tramTypeDao = database.tramTypeDao()

        binding.buttonCancelNewTramType.setOnClickListener {
            finish()
        }

        binding.buttonSaveNewTramType.setOnClickListener {
            val tramTypeName = binding.editTextTramTypeName.text.toString()
            val numberOfAxlesStr = binding.editTextNumberOfAxles.text.toString()

            if (TextUtils.isEmpty(tramTypeName)) {
                binding.editTextTramTypeName.error = "Kérjük, adja meg a jármű típusát!"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(numberOfAxlesStr)) {
                binding.editTextNumberOfAxles.error = "Kérjük, adja meg a tengelyek számát!"
                return@setOnClickListener
            }

            val numberOfAxles = numberOfAxlesStr.toIntOrNull()
            if (numberOfAxles == null || numberOfAxles <= 0) {
                binding.editTextNumberOfAxles.error = "Érvényes számot adjon meg!"
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val newTramType = TramType(name = tramTypeName, numAxles = numberOfAxles)
                tramTypeDao.insert(newTramType)
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@NewTramTypeActivity, MainActivity::class.java).apply {
                        putExtra("NEW_TRAM_TYPE_SUCCESS", "Sikeres jármű felvétel")
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}