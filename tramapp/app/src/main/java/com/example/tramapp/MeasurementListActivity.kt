package com.example.tramapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.tramapp.databinding.ActivityMeasurementListBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeasurementListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeasurementListBinding
    private lateinit var database: AppDatabase
    private lateinit var measurementDao: MeasurementDao
    private lateinit var measurementAdapter: MeasurementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeasurementListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this, lifecycleScope)
        measurementDao = database.measurementDao()

        setupRecyclerView()

        binding.buttonBackToMain.setOnClickListener {
            finish()
        }

        observeMeasurements()
    }

    private fun setupRecyclerView() {
        measurementAdapter = MeasurementAdapter { selectedMeasurement ->
            val intent = Intent(this, MeasurementDetailActivity::class.java).apply {
                putExtra("MEASUREMENT_ID", selectedMeasurement.id)
            }
            startActivity(intent)
        }
        binding.recyclerViewMeasurements.apply {
            layoutManager = LinearLayoutManager(this@MeasurementListActivity)
            adapter = measurementAdapter
        }
    }

    private fun observeMeasurements() {
        lifecycleScope.launch {
            measurementDao.getAll().collectLatest { measurements ->
                measurementAdapter.submitList(measurements)
            }
        }
    }
}