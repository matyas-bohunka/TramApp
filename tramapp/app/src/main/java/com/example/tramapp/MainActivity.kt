package com.example.tramapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.example.tramapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this, lifecycleScope)


        binding.buttonNewMeasurement.setOnClickListener {
            startActivity(Intent(this, NewMeasurementActivity::class.java))
        }

        binding.buttonMeasurements.setOnClickListener {
            startActivity(Intent(this, MeasurementListActivity::class.java))
        }

        binding.buttonNewTramType.setOnClickListener {
            startActivity(Intent(this, NewTramTypeActivity::class.java))
        }

        // Check for messages passed from other activities
        intent.getStringExtra("NEW_TRAM_TYPE_SUCCESS")?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
        intent.getStringExtra("NEW_MEASUREMENT_SUCCESS")?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }
}